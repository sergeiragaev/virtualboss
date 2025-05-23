package net.virtualboss.field.web.controller.v1;

import lombok.RequiredArgsConstructor;
import net.virtualboss.field.service.FieldService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class FieldController {

    private final FieldService fieldService;

    @GetMapping("/fieldcaptions")
    public ResponseEntity<Map<String, String>> fieldCaptions(@RequestParam String fields) {
        return ResponseEntity.ok(fieldService.getFieldCaptions(fields));
    }
}
