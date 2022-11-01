package com.java2nb.novel.service.ex;

public class NameAuthorDuplicated extends ServiceException{
    public NameAuthorDuplicated() {
        super();
    }

    public NameAuthorDuplicated(String message) {
        super(message);
    }

    public NameAuthorDuplicated(String message, Throwable cause) {
        super(message, cause);
    }

    public NameAuthorDuplicated(Throwable cause) {
        super(cause);
    }

    protected NameAuthorDuplicated(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
