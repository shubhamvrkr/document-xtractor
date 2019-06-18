package com.infosys.docxtract.storage;

import java.util.*;
import com.infosys.docxtract.models.*;
import java.io.*;

public abstract class AbstractStorage
{
    public abstract void createTemplateStorage(final String p0);
    
    public abstract List<DocumentReference> getAllTemplates(final String p0);
    
    public abstract File saveTemplate(final Document p0, final String p1);
    
    public abstract Document getTemplateById(final String p0);
}