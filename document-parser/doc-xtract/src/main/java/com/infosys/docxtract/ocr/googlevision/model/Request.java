package com.infosys.docxtract.ocr.googlevision.model;

import lombok.*;

import java.util.*;

@EqualsAndHashCode
@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Request
{
    List<OCRRequest> requests;
}