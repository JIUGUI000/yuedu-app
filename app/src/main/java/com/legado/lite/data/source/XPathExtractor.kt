package com.legado.lite.data.source

import org.xml.sax.InputSource
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.StringReader

/**
 * 极简 XPath 提取器，基于 JDK 自带 javax.xml。
 * 仅支持常见的 `//tag`、`//tag[@attr=val]`、`//tag/text()`、`//tag/@attr`。
 */
object XPathExtractor {

    fun first(path: String, xml: String): String {
        val nl = eval(path, xml) ?: return ""
        val n = nl.item(0) ?: return ""
        return stringify(n)
    }

    fun all(path: String, xml: String): List<String> {
        val nl = eval(path, xml) ?: return emptyList()
        val out = mutableListOf<String>()
        for (i in 0 until nl.length) {
            val s = stringify(nl.item(i))
            if (s.isNotEmpty()) out.add(s)
        }
        return out
    }

    private fun eval(path: String, xml: String): NodeList? {
        return try {
            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = false
                isValidating = false
            }
            // 关闭外部实体
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(InputSource(StringReader(xml)))
            val xp = XPathFactory.newInstance().newXPath()
            xp.evaluate(path, doc, javax.xml.xpath.XPathConstants.NODESET) as? NodeList
        } catch (_: Throwable) { null }
    }

    private fun stringify(node: Node?): String = when {
        node == null -> ""
        node.nodeType == Node.TEXT_NODE || node.nodeType == Node.CDATA_SECTION_NODE -> node.nodeValue ?: ""
        node.nodeType == Node.ATTRIBUTE_NODE -> node.nodeValue ?: ""
        node.nodeType == Node.ELEMENT_NODE -> {
            // 如果是 text() 节点就取文本
            val childText = collectText(node)
            if (childText.isNotEmpty()) childText else node.textContent ?: ""
        }
        else -> node.textContent ?: ""
    }

    private fun collectText(node: Node): String {
        val sb = StringBuilder()
        val children = node.childNodes
        for (i in 0 until children.length) {
            val c = children.item(i)
            if (c.nodeType == Node.TEXT_NODE || c.nodeType == Node.CDATA_SECTION_NODE) {
                sb.append(c.nodeValue ?: "")
            }
        }
        return sb.toString()
    }
}
