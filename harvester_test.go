package CaptchaHarvester

import (
	"fmt"
	"github.com/skateboard/captcha-harvester/script"
	"testing"
	"time"
)

func TestHarvester(t *testing.T) {
	harv := New("test 1", "https://accounts.hcaptcha.com/demo", script.HcaptchaScript)
	go harv.Initialize()

	time.Sleep(5 * time.Second)

	solv, err := harv.Solve("a5f74b19-9e45-40e0-b45d-47ff91b7a6c2")
	if err != nil {
		t.Error(err)
	}
	fmt.Println(solv)

	harv.Destroy()
}
