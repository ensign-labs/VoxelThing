package io.bluestaggo.pds;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class StructureItem {
	private static final List<Class<? extends StructureItem>> REGISTERED_TYPES = List.of(
			ByteItem.class,
			ShortItem.class,
			IntItem.class,
			LongItem.class,
			FloatItem.class,
			DoubleItem.class,
			ByteArrayItem.class,
			StringItem.class,
			ListItem.class,
			CompoundItem.class
	);

	protected static String readString(DataInputStream stream) throws IOException {
		int length = stream.readUnsignedShort();
		byte[] bytes = stream.readNBytes(length);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	protected static void writeString(String string, DataOutputStream stream) throws IOException {
		byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
		stream.writeShort(bytes.length);
		stream.write(bytes);
	}

	@Override
	public String toString() {
		return getString();
	}

	private String getUnsupportedMessage(String type) {
		return "\"" + getClass().getSimpleName() + "\" does not contain \"" + type + "\"";
	}

	public String getString() {
		throw new UnsupportedOperationException(getUnsupportedMessage("String"));
	}

	public byte getByte() {
		throw new UnsupportedOperationException(getUnsupportedMessage("byte"));
	}

	public short getShort() {
		throw new UnsupportedOperationException(getUnsupportedMessage("short"));
	}

	public int getInt() {
		throw new UnsupportedOperationException(getUnsupportedMessage("int"));
	}

	public int getUnsignedByte() {
		return getByte() & 0xFF;
	}

	public int getUnsignedShort() {
		return getShort() & 0xFFFF;
	}

	public long getLong() {
		throw new UnsupportedOperationException(getUnsupportedMessage("long"));
	}

	public long getUnsignedInt() {
		return getInt() & 0xFFFFFFFFL;
	}

	public boolean getBoolean() {
		return getByte() != 0;
	}

	public float getFloat() {
		throw new UnsupportedOperationException(getUnsupportedMessage("float"));
	}

	public double getDouble() {
		throw new UnsupportedOperationException(getUnsupportedMessage("double"));
	}

	public int getType() {
		return REGISTERED_TYPES.indexOf(getClass()) + 1;
	}

	protected abstract void read(DataInputStream stream) throws IOException;

	protected abstract void write(DataOutputStream stream) throws IOException;

	public static StructureItem readItem(DataInputStream stream) throws IOException {
		int type = stream.readUnsignedByte();

		if (type == 0 || type > REGISTERED_TYPES.size()) {
			return null;
		}

		try {
			StructureItem item = REGISTERED_TYPES.get(type - 1).getDeclaredConstructor().newInstance();
			item.read(stream);
			return item;
		} catch (NoSuchMethodException
				| InstantiationException
				| IllegalAccessException
				| IllegalArgumentException
				| java.lang.reflect.InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void writeItem(StructureItem item, DataOutputStream stream) throws IOException {
		stream.write(item.getType());
		item.write(stream);
	}
}
