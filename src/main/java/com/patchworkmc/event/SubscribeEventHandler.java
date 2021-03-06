package com.patchworkmc.event;

import java.util.function.Consumer;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import com.patchworkmc.Patchwork;

public class SubscribeEventHandler extends AnnotationVisitor {
	Consumer<SubscribeEvent> consumer;
	SubscribeEvent instance;

	public SubscribeEventHandler(int access, String name, String descriptor, String signature, boolean hasReturnValue, Consumer<SubscribeEvent> consumer) {
		super(Opcodes.ASM7);

		this.consumer = consumer;
		this.instance = new SubscribeEvent(access, name, descriptor, signature, hasReturnValue);
	}

	@Override
	public void visit(final String name, final Object value) {
		super.visit(name, value);

		if (name.equals("receiveCancelled")) {
			instance.receiveCancelled = value == Boolean.TRUE;
		} else {
			Patchwork.LOGGER.error("Unexpected SubscribeEvent property: " + name + "->" + value);
		}
	}

	@Override
	public void visitEnum(final String name, final String descriptor, final String value) {
		super.visitEnum(name, descriptor, value);

		if (!name.equals("priority")) {
			Patchwork.LOGGER.error("Unexpected SubscribeEvent enum property: " + name + "->" + descriptor + "::" + value);

			return;
		}

		if (!descriptor.equals("Lnet/minecraftforge/eventbus/api/EventPriority;")) {
			Patchwork.LOGGER.error("Unexpected descriptor for SubscribeEvent priority property, continuing anyways: " + descriptor);
		}

		instance.priority = value;
	}

	@Override
	public void visitEnd() {
		super.visitEnd();

		consumer.accept(instance);
	}
}
