package com.kiidlibrary.watcher.configurations;

import feign.RequestTemplate;
import feign.codec.*;
import feign.form.*;
import feign.form.spring.*;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.web.multipart.*;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

public class FeignSpringFormEncoder extends FormEncoder {
    /**
     * Constructor with the default Feign's encoder as a delegate.
     */
    public FeignSpringFormEncoder() {
        this(new Default());
    }

    /**
     * Constructor with specified delegate encoder.
     *
     * @param delegate delegate encoder, if this encoder couldn't encode object.
     */
    public FeignSpringFormEncoder(Encoder delegate) {
        super(delegate);
        MultipartFormContentProcessor processor = (MultipartFormContentProcessor) getContentProcessor(ContentType.MULTIPART);
        processor.addWriter(new SpringSingleMultipartFileWriter());
        processor.addWriter(new SpringManyMultipartFilesWriter());
    }

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        if (bodyType.equals(MultipartFile.class)) {
            MultipartFile file = (MultipartFile) object;
            Map data = Collections.singletonMap(file.getName(), object);
            super.encode(data, MAP_STRING_WILDCARD, template);
            return;
        } else if (bodyType.equals(MultipartFile[].class)) {
            MultipartFile[] file = (MultipartFile[]) object;
            if(file != null) {
                Map data = Collections.singletonMap(file.length == 0 ? "" : file[0].getName(), object);
                super.encode(data, MAP_STRING_WILDCARD, template);
                return;
            }
        }
        super.encode(object, bodyType, template);
    }
}