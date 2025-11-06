package com.example.demo.web;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("f_csrf")
public class CrsfController {
	
	@GetMapping("/csrf")
    public CsrfToken csrfToken(CsrfToken token) {
        return token;
    }
	
}
