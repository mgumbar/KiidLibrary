package com.cloud.kiidlibrary.dal;

import com.cloud.kiidlibrary.model.Kiid;

public interface KiidDAL {
    Kiid getByCloudId(String id);
}
