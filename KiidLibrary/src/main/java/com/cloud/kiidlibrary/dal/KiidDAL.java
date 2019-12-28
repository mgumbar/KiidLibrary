package com.cloud.kiidlibrary.dal;

import com.cloud.kiidlibrary.model.Kiid;
import java.util.List;

public interface KiidDAL {
    Kiid getByCloudId(String id);
}
