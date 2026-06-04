package com.yan.xiaoyanqtserve.service.impl.wechat;

import com.yan.xiaoyanqtserve.common.dto.wechat.WechatMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信公众号 XML 消息解析器。
 */
@Component
public class WechatMessageParser {

    /**
     * 将微信服务器推送的 XML 消息体解析为统一消息实体。
     *
     * @param xmlBody 微信 XML 消息体
     * @return 微信消息实体
     * @throws IllegalArgumentException 消息体为空或 XML 不合法时抛出
     */
    public WechatMessage parse(String xmlBody) {
        if (!StringUtils.hasText(xmlBody)) {
            throw new IllegalArgumentException("微信回调消息体不能为空");
        }

        try {
            Document document = buildDocument(xmlBody);
            Element root = document.getDocumentElement();
            Map<String, String> values = new HashMap<>();
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    values.put(node.getNodeName(), readNodeText(node));
                }
            }
            return new WechatMessage(values);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IllegalArgumentException("微信回调消息体不是合法 XML", e);
        }
    }

    /**
     * 将微信服务器推送的原始字节消息体按 UTF-8 解析为统一消息实体。
     *
     * @param xmlBody 微信 XML 原始消息体
     * @return 微信消息实体
     * @throws IllegalArgumentException 消息体为空或 XML 不合法时抛出
     */
    public WechatMessage parse(byte[] xmlBody) {
        if (xmlBody == null || xmlBody.length == 0) {
            throw new IllegalArgumentException("微信回调消息体不能为空");
        }
        return parse(new String(xmlBody, StandardCharsets.UTF_8));
    }

    private Document buildDocument(String xmlBody) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        var builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                throw exception;
            }
        });
        return builder.parse(new InputSource(new StringReader(xmlBody)));
    }

    private String readNodeText(Node node) {
        String text = node.getTextContent();
        return text == null ? "" : text.trim();
    }
}
