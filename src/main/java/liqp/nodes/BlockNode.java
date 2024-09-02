package liqp.nodes;

import liqp.TemplateContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static liqp.LValue.BREAK;
import static liqp.LValue.CONTINUE;
import static liqp.LValue.asTemporal;
import static liqp.LValue.isTemporal;
import static liqp.LValue.rubyDateTimeFormat;

public class BlockNode implements LNode {

    private List<LNode> children;
    private final boolean isRootBlock;
    private final String EMPTY_STRING = "";

    public BlockNode() {
        this(false);
    }

    public BlockNode(boolean isRootBlock) {
        this.children = new ArrayList<LNode>();
        this.isRootBlock = isRootBlock;
    }

    public void add(LNode node) {
        children.add(node);
    }

    public List<LNode> getChildren() {
        return new ArrayList<LNode>(children);
    }

    @Override
    public Object render(TemplateContext context) {

        StringBuilder builder = new StringBuilder();

        for (LNode node : children) {

            Object value = node.render(context);

            if(value == null) {
                continue;
            }

            if(value == BREAK || value == CONTINUE) {
                return value;
            } else if (value instanceof List) {
                List<?> list = (List<?>) value;

                for (Object obj : list) {
                    builder.append(asString(obj, context));
                }
            } else if (value.getClass().isArray()) {
                Object[] array = (Object[]) value;
                for (Object obj : array) {
                    builder.append(asString(obj, context));
                }
            } else {
                builder.append(asString(value, context));
            }

            if (builder.length() > context.protectionSettings.maxSizeRenderedString) {
                throw new RuntimeException("rendered string exceeds " + context.protectionSettings.maxSizeRenderedString);
            }
        }

        return builder.toString();
    }

    private String asString(Object value, TemplateContext context) {
        if (isTemporal(value)) {
            ZonedDateTime time = asTemporal(value, context);
            return rubyDateTimeFormat.format(time);
        } else {
            return getValueAsString(value);
        }
    }

    private String mapToString(Map<Object, Object> map) {
        try {
            JSONObject jsonObject = new JSONObject(map);
            return jsonObject.toJSONString();
        }
        catch (Exception exception) {
            System.err.println("Exception occurred converting map to a JSONObject. Returning empty string: " + exception.getMessage());
            return EMPTY_STRING;
        }
    }

    private String listToString(List<Object> list) {
        try {
            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(list);
            return jsonArray.toJSONString();
        }
        catch (Exception exception) {
            System.err.println("Exception occurred converting list to a JSONObject. Returning empty string: " + exception.getMessage());
            return EMPTY_STRING;
        }
    }

    private String getValueAsString(Object value) {
        if (Objects.isNull(value)) {
            return "";
        }
        if (value instanceof Map) {
            return mapToString((Map<Object, Object>) value);
        } else if (value instanceof List || value.getClass().isArray()) {
            return listToString((List<Object>) value);
        } else {
            return String.valueOf(value);
        }
    }
}
