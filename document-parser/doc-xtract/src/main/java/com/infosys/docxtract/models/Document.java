package com.infosys.docxtract.models;

import lombok.*;

import java.util.*;

@EqualsAndHashCode
@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Document
{
    private int pageCount;
    private Map<String, Page> pages;
    private String docCode;
    private Barcode barcode;
}