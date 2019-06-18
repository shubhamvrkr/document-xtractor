package com.infosys.docxtract.models;

import lombok.*;
import org.opencv.core.*;

@EqualsAndHashCode
@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Field
{
    private String name;
    private String value;
    private FieldType type;
    private String section;
    private Rect boundary;
    private String format;

}