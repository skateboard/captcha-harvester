package script

var HcaptchaScript = Script{
	header: `
		window.onload = function() {
			document.head.innerHTML = '';
			document.body.innerHTML = '';
			let script = document.createElement('script');
			script.src = 'https://js.hcaptcha.com/1/api.js?render=explicit';
			script.async = true;
			script.defer = true;
		
			let title = document.createElement('title');
			title.innerText = 'Captcha Harvester';
		
			let captchaDiv = document.createElement('div');
			captchaDiv.id = 'captcha-1';
		
			document.getElementsByTagName('head')[0].appendChild(title);
			document.getElementsByTagName('head')[0].appendChild(script);
			document.getElementsByTagName('body')[0].appendChild(captchaDiv);
    	}
	`,
	loader: `
	document.harv = {
    }
    function onError(err) {
        if (document.harv.resolver) {
            document.harv.resolver("ERROR")
        }
    }
    document.harv.harvest = async (siteKey) => {
      let r = new Promise((x) => (document.harv.resolver = x))
      hcaptcha.render('captcha-1', {
        sitekey: siteKey,
        callback: (response) => {
            if (document.harv.resolver) {
                console.log('response', response)
                document.harv.resolver(response)
            }
        },
        "error-callback": "onError"
      })
      return await r
    }
	`,
}
