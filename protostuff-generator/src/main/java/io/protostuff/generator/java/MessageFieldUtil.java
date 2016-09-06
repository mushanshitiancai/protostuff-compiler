package io.protostuff.generator.java;

import io.protostuff.compiler.model.Enum;
import io.protostuff.compiler.model.*;
import io.protostuff.generator.Formatter;

import java.util.List;

import static io.protostuff.compiler.model.ScalarFieldType.*;
import static io.protostuff.compiler.parser.MessageParseListener.MAP_ENTRY_KEY;
import static io.protostuff.compiler.parser.MessageParseListener.MAP_ENTRY_VALUE;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class MessageFieldUtil {

    public static final String HAS_PREFIX = "has";
    public static final String GETTER_PREFIX = "get";
    public static final String SETTER_PREFIX = "set";
    public static final String LIST = "java.util.List";
    public static final String ITERABLE = "java.lang.Iterable";
    public static final String GETTER_REPEATED_SUFFIX = "List";
    public static final String NULL = "null";
    public static final String MAP_SUFFIX = "Map";
    public static final String PUT_PREFIX = "put";

    public static String getFieldType(Field field) {
        FieldType type = field.getType();
        if (type instanceof ScalarFieldType) {
            ScalarFieldType scalarFieldType = (ScalarFieldType) type;
            return ScalarFieldTypeUtil.getPrimitiveType(scalarFieldType);
        }
        if (type instanceof UserType) {
            UserType userType = (UserType) type;
            return UserTypeUtil.getCanonicalName(userType);
        }
        throw new IllegalArgumentException(field.toString());
    }

    public static String getFieldName(Field field) {
        String name = field.getName();
//        String formattedName = Formatter.toCamelCase(name);
        String formattedName = name;
        if (isReservedKeyword(formattedName)) {
            return formattedName + '_';
        }
        return formattedName;
    }

    public static String getJsonFieldName(Field field) {
        String name = field.getName();
        return Formatter.toCamelCase(name);
    }

    private static boolean isReservedKeyword(String formattedName) {
        return JavaConstants.RESERVED_KEYWORDS.contains(formattedName);
    }

    public static String getFieldGetterName(Field field) {
        String getterName = GETTER_PREFIX + Formatter.toPascalCase(field.getName());
        if ("getClass".equals(getterName)) {
            return getterName + "_";
        }
        return getterName;
    }

    public static String getFieldSetterName(Field field) {
        return SETTER_PREFIX + Formatter.toPascalCase(field.getName());
    }

    public static <T> String getEnumFieldValueGetterName(Field field) {
        return GETTER_PREFIX + Formatter.toPascalCase(field.getName()) + "Value";
    }

    public static <T> String getEnumFieldValueSetterName(Field field) {
        return SETTER_PREFIX + Formatter.toPascalCase(field.getName()) + "Value";
    }

    public static String getFieldCleanerName(Field field) {
        return "clear" + Formatter.toPascalCase(field.getName());
    }

    public static boolean isMessage(Field field) {
        return field.getType() instanceof Message;
    }

    public static String getHasMethodName(Field field) {
        return HAS_PREFIX + Formatter.toPascalCase(field.getName());
    }

    public static String getBuilderSetterName(Field field) {
        return SETTER_PREFIX + Formatter.toPascalCase(field.getName());
    }

    public static String getDefaultValue(Field field) {
        FieldType type = field.getType();
        if (type instanceof ScalarFieldType) {
            return ScalarFieldTypeUtil.getDefaultValue((ScalarFieldType) type);
        }
        if (type instanceof Message) {
            Message m = (Message) type;
            return UserTypeUtil.getCanonicalName(m) + ".getDefaultInstance()";
        }
        if (type instanceof Enum) {
            Enum anEnum = (Enum) type;
            String defaultValue;
            List<EnumConstant> constants = anEnum.getConstants();
            if (constants.isEmpty()) {
                defaultValue = "UNRECOGNIZED";
            } else {
                defaultValue = constants.get(0).getName();
            }
            return UserTypeUtil.getCanonicalName(anEnum) + "." + defaultValue;
        }
        throw new IllegalArgumentException(String.valueOf(type));
    }

    /**
     * Check if field type used to store value in java is nullable type.
     */
    public static boolean isScalarNullableType(Field field) {
        FieldType type = field.getType();
        return STRING.equals(type) || BYTES.equals(type) || type instanceof io.protostuff.compiler.model.Enum;
    }

    public static String getRepeatedFieldType(Field field) {
        FieldType type = field.getType();
        if (type instanceof ScalarFieldType) {
            ScalarFieldType scalarFieldType = (ScalarFieldType) type;
            return LIST + "<" + ScalarFieldTypeUtil.getWrapperType(scalarFieldType) + ">";
        }
        if (type instanceof UserType) {
            UserType userType = (UserType) type;
            return LIST + "<" + UserTypeUtil.getCanonicalName(userType) + ">";
        }
        throw new IllegalArgumentException(field.toString());
    }

    public static String getIterableFieldType(Field field) {
        FieldType type = field.getType();
        if (type instanceof ScalarFieldType) {
            ScalarFieldType scalarFieldType = (ScalarFieldType) type;
            return ITERABLE + "<" + ScalarFieldTypeUtil.getWrapperType(scalarFieldType) + ">";
        }
        if (type instanceof UserType) {
            UserType userType = (UserType) type;
            return ITERABLE + "<" + UserTypeUtil.getCanonicalName(userType) + ">";
        }
        throw new IllegalArgumentException(field.toString());
    }

    public static String getWrapperFieldType(Field field) {
        FieldType type = field.getType();
        if (type instanceof ScalarFieldType) {
            ScalarFieldType scalarFieldType = (ScalarFieldType) type;
            return ScalarFieldTypeUtil.getWrapperType(scalarFieldType);
        }
        if (type instanceof UserType) {
            UserType userType = (UserType) type;
            return UserTypeUtil.getCanonicalName(userType);
        }
        throw new IllegalArgumentException(field.toString());
    }

    public static String getRepeatedFieldGetterName(Field field) {
        if (field.isRepeated()) {
            return GETTER_PREFIX + Formatter.toPascalCase(field.getName()) + GETTER_REPEATED_SUFFIX;
        }
        throw new IllegalArgumentException(field.toString());
    }

    public static String getRepeatedEnumFieldValueGetterName(Field field) {
        if (field.isRepeated()) {
            return GETTER_PREFIX + Formatter.toPascalCase(field.getName()) + "ValueList";
        }
        throw new IllegalArgumentException(field.toString());
    }

    public static String javaRepeatedEnumValueGetterByIndexName(Field field) {
        if (field.isRepeated()) {
            return GETTER_PREFIX + Formatter.toPascalCase(field.getName()) + "Value";
        }
        throw new IllegalArgumentException(field.toString());
    }

    public static String getRepeatedEnumConverterName(Field field) {
        if (field.isRepeated()) {
            return "__" + Formatter.toCamelCase(field.getName()) + "Converter";
        }
        throw new IllegalArgumentException(field.toString());
    }

    public static String getRepeatedFieldSetterName(Field field) {
        if (field.isRepeated()) {
            return SETTER_PREFIX + Formatter.toPascalCase(field.getName());
        }
        throw new IllegalArgumentException(field.toString());
    }

    public static String getRepeatedEnumValueSetterName(Field field) {
        if (field.isRepeated()) {
            return SETTER_PREFIX + Formatter.toPascalCase(field.getName()) + "Value";
        }
        throw new IllegalArgumentException(field.toString());
    }

    public static String repeatedGetCountMethodName(Field field) {
        if (field.isRepeated()) {
            return GETTER_PREFIX + Formatter.toPascalCase(field.getName()) + "Count";
        }
        throw new IllegalArgumentException(field.toString());
    }

    public static String repeatedGetByIndexMethodName(Field field) {
        if (field.isRepeated()) {
            return GETTER_PREFIX + Formatter.toPascalCase(field.getName());
        }
        throw new IllegalArgumentException(field.toString());
    }

    public static String getRepeatedBuilderSetterName(Field field) {
        return SETTER_PREFIX + Formatter.toPascalCase(field.getName()) + "List";
    }

    public static String getBuilderGetterName(Field field) {
        return GETTER_PREFIX + Formatter.toPascalCase(field.getName());
    }

    public static String getRepeatedFieldAdderName(Field field) {
        return "add" + Formatter.toPascalCase(field.getName());
    }

    public static String getRepeatedFieldAddAllName(Field field) {
        return "addAll" + Formatter.toPascalCase(field.getName());
    }

    public static String getRepeatedEnumValueAdderName(Field field) {
        return "add" + Formatter.toPascalCase(field.getName()) + "Value";
    }

    public static String getRepeatedEnumValueAddAllName(Field field) {
        return "addAll" + Formatter.toPascalCase(field.getName()) + "Value";
    }

    public static String toStringPart(Field field) {
        String getterName;
        if (field.isMap()) {
            getterName = getMapGetterName(field);
        } else if (field.isRepeated()) {
            getterName = getRepeatedFieldGetterName(field);
        } else {
            getterName = getFieldGetterName(field);
        }
        return "\"" + getFieldName(field) + "=\" + " + getterName + "()";
    }

    public static String protostuffReadMethod(Field field) {
        FieldType type = field.getType();
        if (!(type instanceof ScalarFieldType)) {
            throw new IllegalArgumentException(String.valueOf(type));
        }
        ScalarFieldType fieldType = (ScalarFieldType) type;
        String name;
        switch (fieldType) {
            case INT32:
                name = "readInt32";
                break;
            case INT64:
                name = "readInt64";
                break;
            case UINT32:
                name = "readUInt32";
                break;
            case UINT64:
                name = "readUInt64";
                break;
            case SINT32:
                name = "readSInt32";
                break;
            case SINT64:
                name = "readSInt64";
                break;
            case FIXED32:
                name = "readFixed32";
                break;
            case FIXED64:
                name = "readFixed64";
                break;
            case SFIXED32:
                name = "readSFixed32";
                break;
            case SFIXED64:
                name = "readSFixed64";
                break;
            case FLOAT:
                name = "readFloat";
                break;
            case DOUBLE:
                name = "readDouble";
                break;
            case BOOL:
                name = "readBool";
                break;
            case STRING:
                name = "readString";
                break;
            case BYTES:
                name = "readBytes";
                break;
            default:
                throw new IllegalArgumentException(String.valueOf(type));
        }
        return name;
    }

    public static String protostuffWriteMethod(Field field) {
        FieldType type = field.getType();
        if (!(type instanceof ScalarFieldType)) {
            throw new IllegalArgumentException(String.valueOf(type));
        }
        ScalarFieldType fieldType = (ScalarFieldType) type;
        String name;
        switch (fieldType) {
            case INT32:
                name = "writeInt32";
                break;
            case INT64:
                name = "writeInt64";
                break;
            case UINT32:
                name = "writeUInt32";
                break;
            case UINT64:
                name = "writeUInt64";
                break;
            case SINT32:
                name = "writeSInt32";
                break;
            case SINT64:
                name = "writeSInt64";
                break;
            case FIXED32:
                name = "writeFixed32";
                break;
            case FIXED64:
                name = "writeFixed64";
                break;
            case SFIXED32:
                name = "writeSFixed32";
                break;
            case SFIXED64:
                name = "writeSFixed64";
                break;
            case FLOAT:
                name = "writeFloat";
                break;
            case DOUBLE:
                name = "writeDouble";
                break;
            case BOOL:
                name = "writeBool";
                break;
            case STRING:
                name = "writeString";
                break;
            case BYTES:
                name = "writeBytes";
                break;
            default:
                throw new IllegalArgumentException(String.valueOf(type));
        }
        return name;
    }

    public static String bitFieldName(Field field) {
        return "__bitField" + (field.getIndex() - 1) / 32;
    }

    public static int bitFieldIndex(Field field) {
        return (field.getIndex() - 1) % 32;
    }

    public static int bitFieldMask(Field field) {
        return 1 << bitFieldIndex(field);
    }

    public static String getMapFieldType(Field field) {
        String k = getMapFieldKeyType(field);
        String v = getMapFieldValueType(field);
        return "java.util.Map<" + k + ", " + v + ">";
    }

    public static String getMapFieldKeyType(Field field) {
        FieldType type = field.getType();
        if (!(type instanceof Message)) {
            throw new IllegalArgumentException(field.toString());
        }
        Message entryType = (Message) type;
        ScalarFieldType keyType = (ScalarFieldType) entryType.getField(MAP_ENTRY_KEY).getType();
        return ScalarFieldTypeUtil.getWrapperType(keyType);
    }

    public static String getMapFieldValueType(Field field) {
        FieldType type = field.getType();
        if (!(type instanceof Message)) {
            throw new IllegalArgumentException(field.toString());
        }
        Message entryType = (Message) type;
        Type valueType = entryType.getField(MAP_ENTRY_VALUE).getType();
        String v;
        if (valueType instanceof ScalarFieldType) {
            ScalarFieldType vType = (ScalarFieldType) valueType;
            v = ScalarFieldTypeUtil.getWrapperType(vType);
        } else {
            UserType userType = (UserType) valueType;
            v = UserTypeUtil.getCanonicalName(userType);
        }
        return v;
    }

    public static String getMapGetterName(Field field) {
        if (field.isMap()) {
            return GETTER_PREFIX + Formatter.toPascalCase(field.getName()) + MAP_SUFFIX;
        }
        throw new IllegalArgumentException(field.toString());
    }

    public static String getMapSetterName(Field field) {
        if (field.isMap()) {
            return SETTER_PREFIX + Formatter.toPascalCase(field.getName()) + MAP_SUFFIX;
        }
        throw new IllegalArgumentException(field.toString());
    }

    public static String mapGetByKeyMethodName(Field field) {
        if (field.isMap()) {
            return GETTER_PREFIX + Formatter.toPascalCase(field.getName());
        }
        throw new IllegalArgumentException(field.toString());
    }

    public static String getMapFieldAdderName(Field field) {
        if (field.isMap()) {
            return PUT_PREFIX + Formatter.toPascalCase(field.getName());
        }
        throw new IllegalArgumentException(field.toString());
    }

    public static String getMapFieldAddAllName(Field field) {
        if (field.isMap()) {
            return "putAll" + Formatter.toPascalCase(field.getName());
        }
        throw new IllegalArgumentException(field.toString());
    }

    public static String javaOneofConstantName(Field field) {
        String name = field.getName();
        String underscored = Formatter.toUnderscoreCase(name);
        return Formatter.toUpperCase(underscored);
    }

    public static boolean isNumericType(Field field) {
        FieldType type = field.getType();
        boolean scalar = type instanceof ScalarFieldType;
        return scalar && !(BOOL.equals(type) || STRING.equals(type) || BYTES.equals(type));
    }

    public static boolean isBooleanType(Field field) {
        return BOOL.equals(field.getType());
    }

}
