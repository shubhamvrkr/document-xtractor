package com.infosys.docxtract.models;

import lombok.*;

@EqualsAndHashCode
@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentReference
{
    String barcode;
    String templatePath;
}