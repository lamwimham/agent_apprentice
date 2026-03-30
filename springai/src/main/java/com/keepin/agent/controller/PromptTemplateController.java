package com.keepin.agent.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/promptTemplate")
public class PromptTemplateController {

    @RequestMapping(value = "/call")
    public String call() {
        return "promptTemplate call";
    }

}
