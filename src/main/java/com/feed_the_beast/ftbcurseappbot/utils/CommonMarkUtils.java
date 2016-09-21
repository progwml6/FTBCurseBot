package com.feed_the_beast.ftbcurseappbot.utils;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.html.HtmlRenderer;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import java.util.Arrays;
import java.util.List;

public class CommonMarkUtils {
    private Parser parser;
    private HtmlRenderer renderer;

    public CommonMarkUtils () {
        List<Extension> extensions = Arrays.asList(TablesExtension.create(), StrikethroughExtension.create());
        parser = Parser.builder().extensions(extensions).build();
        renderer = HtmlRenderer.builder().extensions(extensions).build();

    }

    public String renderToHTML (String md) {
        Node document = parser.parse(md);
        return renderer.render(document);

    }
}
