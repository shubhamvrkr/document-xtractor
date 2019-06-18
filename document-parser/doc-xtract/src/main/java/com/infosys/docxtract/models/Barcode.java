package com.infosys.docxtract.models;

import lombok.*;
import org.opencv.core.*;

@EqualsAndHashCode
@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Barcode
{
    private String pageId;
    private Rect boundary;
}