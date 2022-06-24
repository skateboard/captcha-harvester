package CaptchaHarvester

import (
	"context"
	"fmt"
	"github.com/chromedp/cdproto/runtime"
	"github.com/chromedp/chromedp"
	"github.com/skateboard/captcha-harvester/script"
	"time"
)

type Harvester struct {
	name string

	url    string
	script script.Script

	queue *Queue

	ctx context.Context
}

type Solve struct {
	SiteKey string
	Channel chan SolveResult
}

type SolveResult struct {
	Error error
	Token string
}

func New(name, url string, s script.Script) *Harvester {
	return &Harvester{
		name:   name,
		url:    url,
		queue:  NewQueue(),
		script: s,
	}
}

func (h *Harvester) Initialize() error {
	opts := append(chromedp.DefaultExecAllocatorOptions[:],
		chromedp.DisableGPU,
		chromedp.Flag("headless", false),
		chromedp.WindowSize(200, 700),
	)
	allocCtx, cancel := chromedp.NewExecAllocator(context.Background(), opts...)
	defer cancel()
	ctx, cancel2 := chromedp.NewContext(allocCtx)
	defer cancel2()

	if err := chromedp.Run(ctx,
		chromedp.EvaluateAsDevTools(fmt.Sprintf(`window.location = "%v"`, h.url), nil),
		chromedp.EvaluateAsDevTools(h.script.Header(), nil),
		chromedp.EvaluateAsDevTools(h.script.Loader(), nil),
	); err != nil {
		return err
	}

	h.ctx = ctx

	go h.startQueue()
	select {
	case <-ctx.Done():
	}

	return nil
}

func (h *Harvester) Solve(siteKey string) (string, error) {

	resultChannel := make(chan SolveResult)
	h.queue.Push(Solve{
		SiteKey: siteKey,
		Channel: resultChannel,
	})

	resultParsed := <-resultChannel

	if resultParsed.Error != nil {
		return "", resultParsed.Error
	}

	return resultParsed.Token, nil
}

func (h *Harvester) executeHarvest(solve Solve) SolveResult {
	var result string
	if err := chromedp.Run(h.ctx,
		chromedp.Evaluate(fmt.Sprintf(`document.harv.harvest("%s")`, solve.SiteKey), &result, func(p *runtime.EvaluateParams) *runtime.EvaluateParams {
			return p.WithAwaitPromise(true)
		}),
	); err != nil {
		return SolveResult{
			Error: err,
		}
	}

	return SolveResult{
		Token: result,
		Error: nil,
	}
}

func (h *Harvester) Destroy() error {
	err := chromedp.Cancel(h.ctx)
	if err != nil {
		return err
	}

	return nil
}

func (h *Harvester) startQueue() {
	for {
		if h.queue.Len() != 0 {
			firstElement := h.queue.Pop()
			if firstElement == nil {
				time.Sleep(250 * time.Millisecond)
				continue
			}

			parsedElement := firstElement.(Solve)
			if parsedElement.SiteKey == "" {
				time.Sleep(250 * time.Millisecond)
				continue
			}

			result := h.executeHarvest(parsedElement)
			parsedElement.Channel <- result
		} else {
			time.Sleep(250 * time.Millisecond)
			continue
		}
	}
}
