package com.java2nb.novel.foreignSpider;

import com.java2nb.novel.service.ex.NameAuthorDuplicated;
import com.java2nb.novel.service.ex.ServiceException;
import io.github.xxyopen.model.resp.IResultCode;
import io.github.xxyopen.model.resp.RestResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

public class ExController {

    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public RestResult result(Throwable e) {
        if (e instanceof NameAuthorDuplicated) {
            return RestResult.error();
        }


        return RestResult.error();
    }

}
