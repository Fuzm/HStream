package com.stream.client.parser;

import org.jsoup.nodes.Document;

/**
 * Created by Seven-one on 2017/9/25.
 */

public interface Parser<E> {

    E parse(Document d);
}
