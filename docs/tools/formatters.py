from markdown import util as md_util
import xml.etree.ElementTree as etree

def inline_span_format(source="", language="gui", class_name="guilabel", md=None):
    """Inline generic span formatter."""
    el = etree.Element('span', {'class': class_name})
    el.text = md_util.AtomicString(source.replace(">", "‣"))
    return el
