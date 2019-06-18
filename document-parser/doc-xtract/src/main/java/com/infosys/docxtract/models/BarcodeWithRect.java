package com.infosys.docxtract.models;

import java.awt.image.*;

import lombok.*;
import org.opencv.core.*;

@EqualsAndHashCode
@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BarcodeWithRect
{
    private BufferedImage image;
    private Rect rect;
}