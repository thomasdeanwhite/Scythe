package com.scythe.instrumenter.instrumentation.modifiers;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.scythe.instrumenter.instrumentation.objectrepresentation.Branch;
import com.scythe.instrumenter.instrumentation.objectrepresentation.BranchHit;
import com.scythe.instrumenter.instrumentation.visitors.ArrayClassVisitor;

public class ArrayBranchVisitor extends MethodVisitor {
	private ArrayClassVisitor parent;
	private String className;
	private String methodName;
	private int currentLine;

	public ArrayBranchVisitor(ArrayClassVisitor parent, MethodVisitor mv, String className, String methodName,
			String desc, int access) {
		super(Opcodes.ASM5, mv);
		this.className = className;
		this.methodName = methodName;
		this.parent = parent;
	}

	@Override
	public void visitCode() {
		super.visitCode();
	}

	public abstract class BranchDistanceCalculator {
		public abstract void calculateBranchDistance(ArrayBranchVisitor abv);
	}

	public class ConstantBranchDistanceCalculator extends
												  BranchDistanceCalculator {
		private float constant = 0f;

		public ConstantBranchDistanceCalculator (float c){
			constant = c;
		}

		@Override
		public void calculateBranchDistance(ArrayBranchVisitor abv) {
			abv.visitInsn(Opcodes.POP);
			abv.visitInsn(Opcodes.POP);

		    abv.visitLdcInsn(constant);
		}
	}

	public class NumberBranchDistanceCalculator extends
												 BranchDistanceCalculator {

		private int opcode = 0;
		private float offset = 0;
		private int opcodeConversion = 0;
		private int offsetOpcode = 0;

		public NumberBranchDistanceCalculator(int opcode, float offset, int
				opcodeConversion, int offsetOpcode){
			this.opcode = opcode;
			this.offset = offset;
			this.opcodeConversion = opcodeConversion;
			this.offsetOpcode = offsetOpcode;
		}

		@Override
		public void calculateBranchDistance(ArrayBranchVisitor abv) {
			//abv.visitInsn(Opcodes.DUP2);
			visitInsn(opcode);

			if (opcodeConversion > 0) {
				abv.visitInsn(opcodeConversion);
			}

			if (offset > 0) {
				abv.visitLdcInsn(offset);
				abv.visitInsn(offsetOpcode);
			}

		}
	}

	//Zero branch distance calculation
	private ConstantBranchDistanceCalculator zbdc = new
			ConstantBranchDistanceCalculator(0);

	//One branch distance calculation
	private ConstantBranchDistanceCalculator obdc = new
			ConstantBranchDistanceCalculator(0);

	private NumberBranchDistanceCalculator ibdcglte = new
			NumberBranchDistanceCalculator(Opcodes.ISUB,
			0, Opcodes.I2F, 0);

	private NumberBranchDistanceCalculator ibdcgt = new
			NumberBranchDistanceCalculator(Opcodes.ISUB,
			1, Opcodes.I2F, Opcodes.FADD);

	private NumberBranchDistanceCalculator ibdclt = new
			NumberBranchDistanceCalculator(Opcodes.ISUB,
			1, Opcodes.I2F, Opcodes.FSUB);

	private NumberBranchDistanceCalculator fbdcglte = new
			NumberBranchDistanceCalculator(Opcodes.FSUB,
			0, 0, 0);

	private NumberBranchDistanceCalculator fbdcgt = new
			NumberBranchDistanceCalculator(Opcodes.FSUB,
			1, 0, Opcodes.FADD);

	private NumberBranchDistanceCalculator fbdclt = new
			NumberBranchDistanceCalculator(Opcodes.FSUB,
			1, 0, Opcodes.FSUB);


	private NumberBranchDistanceCalculator dbdcglte = new
			NumberBranchDistanceCalculator(Opcodes.DSUB,
			0, Opcodes.D2F, 0);

	private NumberBranchDistanceCalculator dbdcgt = new
			NumberBranchDistanceCalculator(Opcodes.DSUB,
			1, Opcodes.D2F, Opcodes.FADD);

	private NumberBranchDistanceCalculator dbdclt = new
			NumberBranchDistanceCalculator(Opcodes.DSUB,
			1, Opcodes.D2F, Opcodes.FSUB);

	private NumberBranchDistanceCalculator lbdcglte = new
			NumberBranchDistanceCalculator(Opcodes.LSUB,
			0, Opcodes.L2F, 0);

	private NumberBranchDistanceCalculator lbdcgt = new
			NumberBranchDistanceCalculator(Opcodes.LSUB,
			1, Opcodes.L2F, Opcodes.FADD);

	private NumberBranchDistanceCalculator lbdclt = new
			NumberBranchDistanceCalculator(Opcodes.LSUB,
			1, Opcodes.L2F, Opcodes.FSUB);

    private BranchDistanceCalculator bdc = null;

    private BranchHit trueBranch = null;
    private BranchHit falseBranch = null;

	@Override
	public void visitTableSwitchInsn(int i, int i1, Label label,
									 Label... labels) {
		//TODO: Switch Statement
		super.visitTableSwitchInsn(i, i1, label, labels);
	}



	@Override
	public void visitJumpInsn(int opcode, Label label) {
		// TODO Auto-generated method stub

		boolean singleStackElement = false;

		switch (opcode) {
			case Opcodes.IF_ICMPEQ:
			//case Opcodes.IFEQ:
			case Opcodes.IF_ICMPGE:
			//case Opcodes.IFGE:
			case Opcodes.IF_ICMPLE:
			//case Opcodes.IFLE:
                bdc = ibdcglte;
				break;
			case Opcodes.IF_ICMPLT:
			//case Opcodes.IFLT:
                bdc = ibdclt;
				break;
			case Opcodes.IF_ICMPNE:
			//case Opcodes.IFNE:
                bdc = obdc;
				break;
			case Opcodes.IF_ICMPGT:
			//case Opcodes.IFGT:
                bdc = ibdcgt;
				break;
//			case Opcodes.IF_ACMPEQ:
//			case Opcodes.IF_ACMPNE:
//			case Opcodes.IFNONNULL:
//			case Opcodes.IFNULL:
//				bdc = obdc;
//				break;
            case Opcodes.IFGE:
            case Opcodes.IFLE:
            case Opcodes.IFGT:
            case Opcodes.IFLT:
            case Opcodes.IFEQ:
            case Opcodes.IFNE:

                // could be because of a float, double, or long comparison!
                if (trueBranch != null &&
                        falseBranch != null &&
                        bdc != null){
                    //definitely because of comparison
                    visitBranch(opcode, label, trueBranch, falseBranch, bdc,
                            false, false);
                    trueBranch = null;
                    falseBranch = null;
                    bdc = null;

                } else {
                	// only one element on the stack
                    bdc = ibdcglte;
                    //super.visitLdcInsn(0);
                    singleStackElement = true;
                }
                break;
			default:
				super.visitJumpInsn(opcode, label);
				return;
		}


		if (bdc != null) {
			BranchHit trueBranch = setupBranch();
			BranchHit falseBranch = setupBranch();

			visitBranch(opcode, label, trueBranch, falseBranch, bdc, true, singleStackElement);
		}
	}

    @Override
    public void visitInsn(int opcode) {

	    BranchDistanceCalculator bdc = null;

        switch (opcode) {
            case Opcodes.FCMPG:
                bdc = fbdcgt;
                break;
            case Opcodes.FCMPL:
                bdc = fbdclt;
                break;
            case Opcodes.LCMP:
                bdc = lbdcglte;
                break;
            case Opcodes.DCMPG:
                bdc = dbdcgt;
                break;
            case Opcodes.DCMPL:
                bdc = dbdclt;
                break;

        }

        if (bdc != null) {
            BranchHit trueBranch = setupBranch();
            parent.addBranchHit(trueBranch);

            BranchHit falseBranch = setupBranch();
            parent.addBranchHit(falseBranch);

			super.visitInsn(Opcodes.DUP2);
			super.visitInsn(Opcodes.DUP2);

            executeBranchDistance(bdc, trueBranch.getDistanceId());
            executeBranchDistance(bdc, falseBranch.getDistanceId());

            this.bdc = bdc;
        }

        mv.visitInsn(opcode);
    }

	public BranchHit setupBranch(){
		return new BranchHit(new Branch(className, methodName, currentLine),
				parent.newCounterId(), parent.newDistanceId());
	}

	public void executeBranchDistance (BranchDistanceCalculator bdc, int
			counter){
        //visitInsn(Opcodes.DUP2);
        //visitInsn(Opcodes.FALOAD);
		bdc.calculateBranchDistance(this);
        visitFieldInsn(Opcodes.GETSTATIC, className, ArrayClassVisitor
                        .DISTANCE_VARIABLE_NAME,
                ArrayClassVisitor.DISTANCE_VARIABLE_DESC);
        visitInsn(Opcodes.SWAP);
        visitLdcInsn(counter);
        visitInsn(Opcodes.SWAP);
		visitInsn(Opcodes.FASTORE);
	}

    public void visitBranch(int opcode, Label label, BranchHit trueBranch,
                            BranchHit falseBranch, BranchDistanceCalculator
                                    bdc, boolean calculateFalseHits,
							boolean singleStackElement){

        parent.addBranchHit(trueBranch);
        parent.addBranchHit(falseBranch);

        Label l = new Label();
        Label l2 = new Label();
        if (singleStackElement){
        	mv.visitInsn(Opcodes.DUP);
		} else {
			mv.visitInsn(Opcodes.DUP2);
		}
        mv.visitJumpInsn(opcode, l);


        visitFieldInsn(Opcodes.GETSTATIC, className, ArrayClassVisitor.COUNTER_VARIABLE_NAME,
                ArrayClassVisitor.COUNTER_VARIABLE_DESC);
        visitLdcInsn(trueBranch.getCounterId());

        visitInsn(Opcodes.DUP2);
        visitInsn(Opcodes.IALOAD);
        visitInsn(Opcodes.ICONST_1);
        visitInsn(Opcodes.IADD);
        visitInsn(Opcodes.IASTORE);

		if (calculateFalseHits) {
			if (singleStackElement){
				visitLdcInsn(0);
			}

			mv.visitInsn(Opcodes.DUP2);

			executeBranchDistance(bdc, falseBranch.getDistanceId());
		}

        if (singleStackElement && !calculateFalseHits){
        	visitLdcInsn(0);
		}


        executeBranchDistance(zbdc, trueBranch.getDistanceId());


        mv.visitJumpInsn(Opcodes.GOTO, l2);
        visitLabel(l);
        visitFieldInsn(Opcodes.GETSTATIC, className, ArrayClassVisitor.COUNTER_VARIABLE_NAME,
                ArrayClassVisitor.COUNTER_VARIABLE_DESC);
        visitLdcInsn(falseBranch.getCounterId());
        visitInsn(Opcodes.DUP2);
        visitInsn(Opcodes.IALOAD);
        visitInsn(Opcodes.ICONST_1);
        visitInsn(Opcodes.IADD);
        visitInsn(Opcodes.IASTORE);
        if (calculateFalseHits) {
			if (singleStackElement){
				visitLdcInsn(0);
			}
			mv.visitInsn(Opcodes.DUP2);

			executeBranchDistance(bdc, trueBranch.getDistanceId());
		}
		if (singleStackElement && !calculateFalseHits){
			visitLdcInsn(0);
		}
        executeBranchDistance(zbdc, falseBranch.getDistanceId());
		mv.visitJumpInsn(Opcodes.GOTO, label);
        visitLabel(l2);
    }

    public void visitBranch(int opcode, Label label, BranchHit trueBranch,
                            BranchHit falseBranch, BranchDistanceCalculator
                                    bdc){
        visitBranch(opcode, label, trueBranch, falseBranch, bdc, true, false);
    }

	@Override
	public void visitLineNumber(int lineNumber, Label label) {
		currentLine = lineNumber;
		mv.visitLineNumber(lineNumber, label);
	}

}
