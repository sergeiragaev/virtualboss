package net.virtualboss.common.web.controller.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainPageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

}
