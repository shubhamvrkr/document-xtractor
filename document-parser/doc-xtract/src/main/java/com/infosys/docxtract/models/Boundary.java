package com.infosys.docxtract.models;

import lombok.*;

@EqualsAndHashCode
@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Boundary
{
    private int x;
    private int y;
    private int width;
    private int height;

}