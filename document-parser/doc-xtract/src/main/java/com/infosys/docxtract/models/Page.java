package com.infosys.docxtract.models;

import lombok.*;

import java.util.*;

@EqualsAndHashCode
@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Page
{
    private String id;
    private int width;
    private int height;
    private String content;
    private List<Field> fields;
}