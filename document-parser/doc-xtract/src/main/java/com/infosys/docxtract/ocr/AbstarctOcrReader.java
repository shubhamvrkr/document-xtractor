package com.infosys.docxtract.ocr;

public abstract class AbstarctOcrReader<T>
{
    public abstract T readTextFromImage(final String p0 , final String type);
}