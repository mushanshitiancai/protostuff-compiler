package io.protostuff.compiler.parser;

import io.protostuff.compiler.model.*;
import io.protostuff.compiler.model.DynamicMessage.Value;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

import static io.protostuff.compiler.model.FieldModifier.*;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class MessageParseListener extends AbstractProtoParserListener {

    public static final String OPTION_MAP_ENTRY = ".google.protobuf.map_entry";
    public static final String MAP_ENTRY_KEY = "key";
    public static final String MAP_ENTRY_VALUE = "value";

    public MessageParseListener(BufferedTokenStream tokens, ProtoContext context) {
        super(tokens, context);
    }

    @Override
    public void enterMessageBlock(ProtoParser.MessageBlockContext ctx) {
        UserTypeContainer parent = context.peek(UserTypeContainer.class);
        Message message = new Message(parent);
        context.push(message);
    }

    @Override
    public void exitMessageBlock(ProtoParser.MessageBlockContext ctx) {
        Message message = context.pop(Message.class);
        MessageContainer container = context.peek(MessageContainer.class);
        String name = ctx.messageName().getText();
        message.setName(name);
        message.setSourceCodeLocation(getSourceCodeLocation(ctx));
        container.addMessage(message);
        attachComments(ctx, message, false);
    }

    @Override
    public void exitReservedFieldRanges(ProtoParser.ReservedFieldRangesContext ctx) {
        Message message = context.peek(Message.class);
        List<Range> result = getRanges(message, ctx.range());
        for (Range range : result) {
            message.addReservedFieldRange(range);
        }
    }

    @Override
    public void exitReservedFieldNames(ProtoParser.ReservedFieldNamesContext ctx) {
        Message message = context.peek(Message.class);
        for (ProtoParser.ReservedFieldNameContext fieldNameContext : ctx.reservedFieldName()) {
            String fieldName = fieldNameContext.getText();
            fieldName = Util.removeFirstAndLastChar(fieldName);
            message.addReservedFieldName(fieldName);
        }
    }

    @Override
    public void enterField(ProtoParser.FieldContext ctx) {
        FieldContainer parent = context.peek(FieldContainer.class);
        Field field = new Field(parent);
        context.push(field);
    }

    @Override
    public void exitField(ProtoParser.FieldContext ctx) {
        Field field = context.pop(Field.class);
        FieldContainer fieldContainer = context.peek(FieldContainer.class);
        String name = ctx.fieldName().getText();
        String type = ctx.typeReference().getText();
        Integer tag = Integer.decode(ctx.tag().getText());
        updateModifier(ctx.fieldModifier(), field);
        field.setName(name);
        field.setTag(tag);
        field.setIndex(fieldContainer.getFieldCount() + 1);
        field.setTypeName(type);
        field.setSourceCodeLocation(getSourceCodeLocation(ctx));
        fieldContainer.addField(field);
        attachComments(ctx, field, true);
    }

    @Override
    public void enterExtendBlock(ProtoParser.ExtendBlockContext ctx) {
        UserTypeContainer parent = context.peek(UserTypeContainer.class);
        Extension extension = new Extension(parent);
        context.push(extension);
    }

    @Override
    public void exitExtendBlock(ProtoParser.ExtendBlockContext ctx) {
        Extension extension = context.pop(Extension.class);
        String extendeeName = ctx.typeReference().getText();
        ExtensionContainer extensionContainer = context.peek(AbstractUserTypeContainer.class);
        extension.setExtendeeName(extendeeName);
        extension.setSourceCodeLocation(getSourceCodeLocation(ctx));
        extensionContainer.addDeclaredExtension(extension);
    }

    @Override
    public void enterGroupBlock(ProtoParser.GroupBlockContext ctx) {
        Element parent = context.peek(Element.class);
        if (parent instanceof Extension) {
            // hack: use extension's parent
            Group group = new Group(((Extension) parent).getParent());
            context.push(group);
        } else if (parent instanceof UserTypeContainer) {
            Group group = new Group(((UserTypeContainer) parent));
            context.push(group);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void exitGroupBlock(ProtoParser.GroupBlockContext ctx) {
        Group group = context.pop(Group.class);
        group.setName(ctx.groupName().getText());
        group.setSourceCodeLocation(getSourceCodeLocation(ctx));
        GroupContainer groupContainer = context.peek(GroupContainer.class);
        FieldContainer fieldContainer = context.peek(FieldContainer.class);
        Field field = new Field(fieldContainer);
        field.setName(group.getName().toLowerCase()); // same behavior as in protoc
        int tag = Integer.decode(ctx.tag().getText());
        field.setTag(tag);
        field.setIndex(fieldContainer.getFieldCount() + 1);
        field.setTypeName(group.getName());
        field.setType(group);
        field.setSourceCodeLocation(getSourceCodeLocation(ctx));
        groupContainer.addGroup(group);
        fieldContainer.addField(field);
        attachComments(ctx, field, true);
    }

    @Override
    public void enterOneof(ProtoParser.OneofContext ctx) {
        Message parent = context.peek(Message.class);
        Oneof oneof = new Oneof(parent);
        context.push(oneof);
    }

    @Override
    public void exitOneof(ProtoParser.OneofContext ctx) {
        Oneof oneof = context.pop(Oneof.class);
        Message message = context.peek(Message.class);
        oneof.setName(ctx.oneofName().getText());
        oneof.setSourceCodeLocation(getSourceCodeLocation(ctx));
        message.addOneof(oneof);
        attachComments(ctx, oneof, false);
    }

    @Override
    public void enterOneofField(ProtoParser.OneofFieldContext ctx) {
        Oneof oneof = context.peek(Oneof.class);
        Message message = oneof.getParent();
        Field field = new Field(message);
        field.setOneof(oneof);
        context.push(field);
    }

    @Override
    public void exitOneofField(ProtoParser.OneofFieldContext ctx) {
        Field field = context.pop(Field.class);
        Oneof oneOf = context.peek(Oneof.class);
        Message message = oneOf.getParent();
        String name = ctx.fieldName().getText();
        String type = ctx.typeReference().getText();
        Integer tag = Integer.decode(ctx.tag().getText());
        field.setName(name);
        field.setTag(tag);
        field.setIndex(message.getFieldCount() + 1);
        field.setTypeName(type);
        field.setSourceCodeLocation(getSourceCodeLocation(ctx));
        oneOf.addField(field);
        message.addField(field);
        attachComments(ctx, field, true);
    }

    @Override
    public void enterOneofGroup(ProtoParser.OneofGroupContext ctx) {
        Oneof parent = context.peek(Oneof.class);
        Group group = new Group(parent.getParent());
        context.push(group);
    }

    @Override
    public void exitOneofGroup(ProtoParser.OneofGroupContext ctx) {
        Group group = context.pop(Group.class);
        group.setSourceCodeLocation(getSourceCodeLocation(ctx));
        GroupContainer container = context.peek(GroupContainer.class);
        container.addGroup(group);
    }

    @Override
    public void enterMap(ProtoParser.MapContext ctx) {
        Message parent = context.peek(Message.class);
        Field field = new Field(parent);
        context.push(field);
    }

    @Override
    public void exitMap(ProtoParser.MapContext ctx) {
        Field field = context.pop(Field.class);
        Message message = context.peek(Message.class);
        String name = ctx.fieldName().getText();
        String keyTypeName = ctx.mapKey().getText();
        String valueTypeName = ctx.mapValue().getText();
        SourceCodeLocation codeLocation = getSourceCodeLocation(ctx);
        Message map = new Message(message);
        String mapEntryTypeName = name + "_entry";
        map.setName(mapEntryTypeName);
        map.setSourceCodeLocation(codeLocation);
        map.getOptions().set(codeLocation, OPTION_MAP_ENTRY, Value.createBoolean(true));
        Field keyField = createMapKeyField(map, keyTypeName, codeLocation);
        map.addField(keyField);
        Field valueField = createMapValueField(map, valueTypeName, codeLocation);
        map.addField(valueField);
        Integer tag = Integer.decode(ctx.tag().getText());
        field.setName(name);
        field.setTag(tag);
        field.setIndex(message.getFieldCount() + 1);
        field.setModifier(REPEATED);
        field.setTypeName(mapEntryTypeName);
        field.setType(map);
        field.setSourceCodeLocation(codeLocation);
        message.addField(field);
        message.addMessage(map);
        attachComments(ctx, field, true);
    }

    private Field createMapValueField(Message map, String valueTypeName, SourceCodeLocation codeLocation) {
        Field valueField = new Field(map);
        valueField.setName(MAP_ENTRY_VALUE);
        valueField.setTag(2);
        valueField.setIndex(2);
        valueField.setModifier(OPTIONAL);
        valueField.setTypeName(valueTypeName);
        valueField.setSourceCodeLocation(codeLocation);
        return valueField;
    }

    private Field createMapKeyField(Message map, String keyTypeName, SourceCodeLocation codeLocation) {
        Field keyField = new Field(map);
        keyField.setName(MAP_ENTRY_KEY);
        keyField.setTag(1);
        keyField.setIndex(1);
        keyField.setTypeName(keyTypeName);
        keyField.setModifier(OPTIONAL);
        keyField.setSourceCodeLocation(codeLocation);
        return keyField;
    }

    @Override
    public void exitExtensions(ProtoParser.ExtensionsContext ctx) {
        Message message = context.peek(Message.class);
        List<Range> result = getRanges(message, ctx.range());
        for (Range range : result) {
            message.addExtensionRange(range);
        }
    }

    private List<Range> getRanges(Message message, List<ProtoParser.RangeContext> ranges) {
        List<Range> result = new ArrayList<>();
        for (ProtoParser.RangeContext rangeContext : ranges) {
            ProtoParser.RangeFromContext fromNode = rangeContext.rangeFrom();
            ProtoParser.RangeToContext toNode = rangeContext.rangeTo();
            TerminalNode maxNode = rangeContext.MAX();
            int from = Integer.decode(fromNode.getText());
            int to;
            if (toNode != null) {
                to = Integer.decode(toNode.getText());
            } else if (maxNode != null) {
                to = Field.MAX_TAG_VALUE;
            } else {
                to = from;
            }
            Range range = new Range(message, from, to);
            range.setSourceCodeLocation(getSourceCodeLocation(rangeContext));
            result.add(range);
        }
        return result;
    }

    private void updateModifier(ProtoParser.FieldModifierContext modifierContext, Field field) {
        if (modifierContext != null) {
            if (modifierContext.OPTIONAL() != null) {
                field.setModifier(OPTIONAL);
            } else if (modifierContext.REQUIRED() != null) {
                field.setModifier(REQUIRED);
            } else if (modifierContext.REPEATED() != null) {
                field.setModifier(REPEATED);
            } else {
                throw new IllegalStateException("not implemented");
            }
        }
    }
}
