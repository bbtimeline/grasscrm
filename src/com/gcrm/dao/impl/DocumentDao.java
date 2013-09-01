/**
 * Copyright (C) 2012 - 2013, Grass CRM Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gcrm.dao.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.gcrm.dao.IDocumentDao;
import com.gcrm.domain.Document;

/**
 * Document DAO
 */
public class DocumentDao extends BaseDao<Document> implements IDocumentDao {

    /*
     * (non-Javadoc)
     * 
     * @see com.gcrm.dao.IDocumentDao#save(com.gcrm.domain.Document,
     * java.io.File)
     */
    @SuppressWarnings("resource")
    public Document save(Document document, File f) throws Exception {
        if (f != null) {
            InputStream stream = new BufferedInputStream(new FileInputStream(f));
            byte[] input = new byte[stream.available()];
            stream.read(input);
            document.setFileContent(input);
        }
        return super.makePersistent(document);
    }

}
