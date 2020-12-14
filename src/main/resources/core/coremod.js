
var MethodInsnNode = org.objectweb.asm.tree.MethodInsnNode;
var VarInsnNode = org.objectweb.asm.tree.VarInsnNode;
var FieldInsnNode = org.objectweb.asm.tree.FieldInsnNode;
var Opcodes = org.objectweb.asm.Opcodes;
var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");

function initializeCoreMod() {
    return {
        "debugrenderer": {
            target: {
                type: "CLASS",
                name: "net/minecraft/client/renderer/debug/DebugRenderer"
            },
            transformer: transformRender
        },
        "debugpacketsender": {
            target: {
                type: "CLASS",
                name: "net/minecraft/network/DebugPacketSender"
            },
            transformer: transformDPS
        },
        "debugticks": {
            target: {
                type: "CLASS",
                name: "net/minecraft/server/MinecraftServer"
            },
            transformer: transformServer
        },
        "debugkey": {
            target: {
                type: "CLASS",
                name: "net/minecraft/client/KeyboardListener"
            },
            transformer: transformF3
        }
    };
}
// func_229019_a_(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer$Impl;DDD)V
// func_218803_a(Lnet/minecraft/world/World;Lnet/minecraft/entity/MobEntity;Lnet/minecraft/pathfinding/Path;F)V
// func_229574_b_()V
// func_71190_q(Ljava/util/function/BooleanSupplier;)V

function transformRender( node ) {
    for( var i in node.methods ) {
        var method = node.methods[ i ];
        if( method.name == ASMAPI.mapMethod("func_229019_a_") && method.desc == "(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer$Impl;DDD)V" ) {
            for( var i = 0; i < method.instructions.size(); i ++ ) {
                var insn = method.instructions.get( i );
                if( insn.getOpcode() == Opcodes.RETURN ) {
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 1));
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 2));
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.DLOAD, 3));
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.DLOAD, 5));
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.DLOAD, 7));
                    method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "net/shadew/ned/hooks/DebugRenderHook", "onRenderDebug", "(Lnet/minecraft/client/renderer/debug/DebugRenderer;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer$Impl;DDD)V", false));
                    break;
                }
            }
        }
    }
    return node;
}

// func_218806_a(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V
function transformDPS(node) {
    for( var i in node.methods ) {
        var method = node.methods[ i ];
        if( method.name == ASMAPI.mapMethod("func_218803_a") && method.desc == "(Lnet/minecraft/world/World;Lnet/minecraft/entity/MobEntity;Lnet/minecraft/pathfinding/Path;F)V" ) {
            for( var i = 0; i < method.instructions.size(); i ++ ) {
                var insn = method.instructions.get( i );
                if( insn.getOpcode() == Opcodes.RETURN ) {
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 1));
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 2));
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.FLOAD, 3));
                    method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "net/shadew/ned/hooks/PathfindingHook", "onSendPath", "(Lnet/minecraft/world/World;Lnet/minecraft/entity/MobEntity;Lnet/minecraft/pathfinding/Path;F)V", false));
                    break;
                }
            }
        }
        if( method.name == ASMAPI.mapMethod("func_218806_a") && method.desc == "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V" ) {
            for( var i = 0; i < method.instructions.size(); i ++ ) {
                var insn = method.instructions.get( i );
                if( insn.getOpcode() == Opcodes.RETURN ) {
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 1));
                    method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "net/shadew/ned/hooks/NeighborUpdateHook", "onSendNeighborUpdate", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", false));
                    break;
                }
            }
        }
    }
    return node;
}

function transformServer( node ) {
    for( var i in node.methods ) {
        var method = node.methods[ i ];
        if( method.name == ASMAPI.mapMethod("func_71190_q") && method.desc == "(Ljava/util/function/BooleanSupplier;)V" ) {
            for( var i = 0; i < method.instructions.size(); i ++ ) {
                var insn = method.instructions.get( i );
                if( insn.getOpcode() == Opcodes.INVOKEVIRTUAL && insn.owner == "net/minecraft/test/TestCollection" && insn.name == ASMAPI.mapMethod("func_229574_b_") && insn.desc == "()V" ) {
                    method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "net/shadew/gametest/test/TestTicker", "tickStatic", "()V", false));
                    break;
                }
            }
        }
    }
    return node;
}

// func_197962_c(I)Z
function transformF3( node ) {
    for( var i in node.methods ) {
        var method = node.methods[ i ];
        if( method.name == ASMAPI.mapMethod("func_197962_c") && method.desc == "(I)Z" ) {
            var skipFirst = true;
            for( var i = 0; i < method.instructions.size(); i ++ ) {
                var insn = method.instructions.get( i );
                if( insn.getOpcode() == Opcodes.IRETURN ) {
                    if( skipFirst ) {
                        skipFirst = false;
                        continue;
                    }
                    i += 2;
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ILOAD, 1));
                    method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "net/shadew/ned/hooks/DebugKeyHook", "onProcessDebugKey", "(ZI)Z", false));
                }
            }
        }
    }
    return node;
}
