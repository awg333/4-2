import java.io.*;
import java.util.Map;
import java.util.TreeMap;

public class Parser {
	static Map<String, Double> symbolTable = new TreeMap<String, Double>();
	static FileReader fin;
	// ���忡 ���Ե� �ĺ���, ����, ������ ����
	static int id = 0;
	static int constant = 0;
	static int operand = 0;
	//����ó�� ����
	static String errorStr = null;
	static int errorCode;
	private static final double ERRORVALUE = Double.MAX_VALUE;
	//lexeme ���� �� ��ū����
	static char[] lexeme = new char[100];	
	static int lexLen = 0;
	static int c;
	static int charClass;
	static int next_token;
	static String token_string;
	
	// Character classes	
	private static final int LETTER = 0;
	private static final int DIGIT = 1;
	private static final int OPERATION = 2;
	private static final int UNKNOWN = 3;
	
	// Token code
	private static final int CONSTANT = 10;
	private static final int IDENT = 11;
	private static final int ASSIGN_OP = 20;
	private static final int ADD_OP = 21;
	private static final int SUB_OP = 22;
	private static final int MULT_OP = 23;
	private static final int DIV_OP = 24;
	private static final int QUESTION_OP = 25;
	private static final int LEFT_PAREN = 26;
	private static final int RIGHT_PAREN = 27;
	private static final int LESS_KEYWORD = 28;
	private static final int GREATER_KEYWORD = 29;
	private static final int EQUAL_KEYWORD = 30;
	private static final int COLON = 31;
	private static final int SEMI_COLON = 32;
	
	 // ���� �м���
	static void lexical() {
		lexLen = 0;
		getNonBlank();
		
		switch(charClass) {
		case LETTER:
			addChar();
			getChar();
			while(charClass == LETTER | charClass == DIGIT) {
				addChar();
				getChar();
			}
			// �ĺ��� ����
			next_token = IDENT;
			id++;
			break;
		case DIGIT:
			addChar();
			getChar();
			while(charClass == DIGIT) {
				addChar();
				getChar();
			}
			// ��� ����
			next_token = CONSTANT;
			constant++;
			break;
		case OPERATION:
			next_token = lookup();
			addChar();
			getChar();
			while((next_token != SEMI_COLON) & (charClass == OPERATION)) {
				addChar();
				getChar();
			}
			// �ߺ������� �� == ��ȣ ó��
			if(lexLen > 1) {
				if(lexLen == 2) {
					boolean equalOp = false;
					for(int i = 0; i<2;i++) {
						if(lexeme[i] == '=') {
							equalOp = true;
						}else {
							equalOp = false;
						}
					}
					if(equalOp) {
						next_token = EQUAL_KEYWORD;
					}else {
						errorStr = String.valueOf(lexeme[lexLen - 1]);
						errorCode = 2;
						lexLen--;
					}
				}
			}
			// ������ ����
			if(next_token == ADD_OP | next_token == SUB_OP | next_token == MULT_OP | next_token == DIV_OP | next_token == QUESTION_OP) operand++;
			break;
		case UNKNOWN:
			getChar();
			break;
		}
		
		token_string = new String(lexeme, 0, lexLen);
		System.out.print(token_string + " ");
	}
	
	// ���� �迭�� �о�� ���� ����
	static void addChar() {
		if(lexLen <= 98) {
			lexeme[lexLen] = (char)c;
			lexLen++;
		}else {
			System.out.println("Error - lexeme is too long");
		}
	}
	
	// ���Ͽ��� ���ڸ� �о��
	static void getChar() {
		try {
			if((c = fin.read()) != -1) {
				if((c >= 65 & c <= 90) | (c >= 97 & c <= 122)) {
					charClass = LETTER;
				}else if(c >= 48 & c <= 57) {
					charClass = DIGIT;
				}else if(c <= 32 | c == -1) {
					charClass = UNKNOWN;
				}else charClass = OPERATION;
			}
		}catch(IOException e) {
			System.out.println("����� ����");

		}
	}
	
	// ���� ����
	static void getNonBlank() {
		while(c <= 32) getChar();
	}
	
	// �������� ��ū Ȯ��
	static int lookup() {
		switch((char)c) {
		case '=':
			return ASSIGN_OP;
		case '+':
			return ADD_OP;
		case '-':
			return SUB_OP;
		case '*':
			return MULT_OP;
		case '/':
			return DIV_OP;
		case '?':
			return QUESTION_OP;
		case '(':
			return LEFT_PAREN;
		case ')':
			return RIGHT_PAREN;
		case '<':
			return LESS_KEYWORD;
		case '>':
			return GREATER_KEYWORD;
		case ':':
			return COLON;
		case ';':
			return SEMI_COLON;
		default:
			return -1;
		}
	}
	
	//<statements> �� <statement> | <statement> <semi_colon> <statements>
	static void parseStatements() {
		// identifier ����
		String ident = token_string;
		
		double result = parseStatement();
		
		// ������ �� -> semicolon �̶�� �ɺ����̺� ������ ���� ���� �� ���
		if(next_token == SEMI_COLON) {
			// l-value ���� ���� ó��
			if(errorCode != 9) symbolTable.put(ident, result);
			//�ɺ����̺� ���� �� ���
			System.out.print(" ==> ID : " + id + "; CONST : " + constant + "; OP : " + operand + " ");
			SyntaxWarning(errorStr, errorCode);
			
			// ����ó�� ���� �� ���� �ʱ�ȭ
			id = 0;
			operand = 0;
			constant = 0;
			errorCode = 0;
		}else {
			// semicolon�� �ƴ϶�� �����ڵ� 3 ���� �� �ɺ����̺� ��� ����
			errorStr = token_string;
			errorCode = 3;
			symbolTable.put(ident, result);
			
			// identifier ���� �� �ٽ� �Ľ�
			ident = token_string;
			result = parseStatement();
			symbolTable.put(ident, result);
			//�ɺ����̺� ���� �� ���
			System.out.print(" ==> ID : " + id + "; CONST : " + constant + "; OP : " + operand + " ");
			SyntaxWarning(errorStr, errorCode);
			
			id = 0;
			operand = 0;
			constant = 0;
			errorCode = 0;
		}
		
	}
	
	// <statement>	�� <ident> <assignment_operator> <expression>
	static double parseStatement() {
		double result = ERRORVALUE;
		if(next_token == IDENT) {
		}else {
			// l-value ���� ����ó��(�����ڵ�: 9)
			errorCode = 9;
			while(next_token != SEMI_COLON) lexical();
			return ERRORVALUE;
		}
		lexical();
		if(next_token == ASSIGN_OP) {
		}else {
			// =�� ������ ���� ������ �̵� �� UNKNOWN ����(�����ڵ�: 8)
			errorCode = 8;
			while(next_token != SEMI_COLON) lexical();
			return ERRORVALUE;
		}
		// �������� ���� ������ ���� Expression���� �Ľ�
		result = parseExpression();
		return result;
	}
	// <expression>	�� <term> <plus_operator> <expression> | <term> <minus_operator> <expression> | <term>
	static double parseExpression() {
		// Term���� �Ľ�
		double result = parseTerm();
		
		// next_token�� +������ �� -�������� ��� ��� ���
		while(next_token == ADD_OP | next_token == SUB_OP) {
			if(next_token == ADD_OP) {
				// �Ʒ� �ܰ迡�� �޾ƿ� result ���� ERRORVALUE�� ��� ���� ����
				if(result == ERRORVALUE) {
					parseTerm();
				}else {
					result += parseTerm();
				}
			}else if(next_token == SUB_OP) {
				if(result == ERRORVALUE) {
					parseTerm();
				}else {
					result -= parseTerm();
				}
			}else {
				break;
			}
		}
		
		
		return result;
	}
	//<term>	�� <factor> <star_operator> <term> | <factor> <slash_operator> <term> | <factor>
	static double parseTerm() {
		// Factor�� �Ľ�
		double result = parseFactor();
		
		// next_token�� *������ �� /�������� ��� ��� ���
		while(next_token == MULT_OP | next_token == DIV_OP) {
			if(next_token == MULT_OP) {
				// �Ʒ� �ܰ迡�� �޾ƿ� result ���� ERRORVALUE�� ��� ���� ����
				if(result == ERRORVALUE) {
					parseFactor();
				}else {
					result *= parseFactor();
				}
			}else if(next_token == DIV_OP) {
				if(result == ERRORVALUE) {
					parseFactor();
				}else {
					result /= parseFactor();
				}
			}else {
				break;
			}
		}
		
		return result;
		
	}
	
	// <factor>	�� <left_parenthesis> <expression> <right_parenthesis> | <ident> | <constant> | <ident><condition><ident><question_operator><compare_value>
	static double parseFactor() {
		double result = ERRORVALUE;
		
		String ident = null;
		lexical();
		
		// next_token�� identifier�� ��� 
		if(next_token == IDENT) {
			ident = token_string;
			// �ɺ����̺��� ���� �޾ƿ� result�� ����
			if(symbolTable.containsKey(token_string)) {
				result = symbolTable.get(token_string);
			}else {
				// �ɺ����̺� ���ٸ� ���ǵ��� ���� ������ ������ ������ ���� ó��(�����ڵ�: 1)
				errorStr = token_string;
				errorCode = 1;
				// result���� ERRORVALUE�� ���� 
				result = ERRORVALUE;
			}
			lexical();
			// next_token�� ����� ���
		}else if(next_token == CONSTANT) {
			try {
				// �� ���� result�� ����
				result = Double.parseDouble(token_string);
				//NumberFormatException���� double���� �Ľ̿� ���� ó��
			}catch(NumberFormatException e) {
				System.out.println("error");
			}
			lexical();
			
			// next_token�� ���� ��ȣ�� ���
		}else if(next_token == LEFT_PAREN) {
			// �ٽ� Expression���� �Ľ�
			result = parseExpression();
			// ���������� ������ ��ȣ�� ��� ���ֺм�
			if(next_token == RIGHT_PAREN) {
				lexical();
			}else {
				// ������ ��ȣ�� ���� ������ ���� ó��(�����ڵ�: 4)
				errorCode = 4;
				
			}

		}
		
		// next_token�� �� �������� ��� condition���� �Ľ�
		if(next_token == LESS_KEYWORD | next_token == GREATER_KEYWORD | next_token == EQUAL_KEYWORD) {
			result = parseCondition(ident);
		}
		
		return result;
	}
	// <condition> 	�� <less_keyword> | <greater_keyword> | <equal_keyword>
	static double parseCondition(String str) {
		double result;
		
		if(next_token == LESS_KEYWORD) {
			lexical();
			// < ������ �� ���� identifier�� �ƴ϶�� ����ó��
			if(next_token != IDENT) {
				// ���� ������ ��� �� ERRORVALUE ����(�����ڵ�: 7)
				errorStr = token_string;
				while(next_token != SEMI_COLON) lexical();
				errorCode = 7;
				return ERRORVALUE;
			}
			// ���� CompartValue�� �Ľ�
			if(symbolTable.containsKey(token_string))	result = parseCompareValue(symbolTable.get(str) < symbolTable.get(token_string));
			else {
				// ���� �ɺ����̺� token_string���� ���ٸ� ���� ���� ���� ���� ��� ����ó��(�����ڵ�: 1) 
				errorStr = token_string;
				while(next_token != SEMI_COLON) lexical();
				errorCode = 1;
				return ERRORVALUE;
			}
		}else if(next_token == GREATER_KEYWORD) {
			lexical();
			if(next_token != IDENT) {
				//����ó��
				errorStr = token_string;

				while(next_token != SEMI_COLON) lexical();
				errorCode = 7;
				return ERRORVALUE;
			}
			// ���� CompareValue�� �Ľ�
			if(symbolTable.containsKey(token_string))	result = parseCompareValue(symbolTable.get(str) > symbolTable.get(token_string));
			else {
				// ���� �ɺ����̺� token_string���� ���ٸ� ���� ���� ���� ���� ��� ����ó��(�����ڵ�: 1) 
				errorStr = token_string;
				while(next_token != SEMI_COLON) lexical();
				errorCode = 1;
				return ERRORVALUE;
			}
		}else {
			lexical();
			if(next_token != IDENT) {
				//����ó��
				errorStr = token_string;
				while(next_token != SEMI_COLON) lexical();
				errorCode = 7;
				return ERRORVALUE;
			}

			// ���� CompartValue�� �Ľ�
			if(symbolTable.containsKey(token_string))	result = parseCompareValue(symbolTable.get(str) == symbolTable.get(token_string));
			else {
				// ���� �ɺ����̺� token_string���� ���ٸ� ���� ���� ���� ���� ��� ����ó��(�����ڵ�: 1) 
				errorStr = token_string;
				while(next_token != SEMI_COLON) lexical();
				errorCode = 1;
				return ERRORVALUE;
			}
		}
		// next_token�� semicolon�̸� ���ֺм��� ���� ����
		if(next_token != SEMI_COLON)	lexical();
		return result;
	}
	// <compare_value> �� <ident> <colon> <ident>(ident : ident�̱� ������ ����� ������� ����)
	static double parseCompareValue(boolean b) {
		double result = ERRORVALUE;
		String ident1 = null;
		String ident2 = null;
		// identifier�� �ϳ� ���ٸ� false
		boolean idCheck = true;
		lexical();
		// ���׿��꿡�� ?�� ���ٸ� 
		if(next_token != QUESTION_OP) {
			// ���� ���ַ� �Ѿ�� �ʰ� ���(�����ڵ�: 5)
			errorCode = 5;
		}else {
			lexical();
		}
		
		if(next_token != IDENT) {
			//next_token�� identifier�� �ƴ϶�� ����ó��(�����ڵ�: 6)
			errorCode = 6;
			// �׸��� idCheck�� false�� �ٲ���
			idCheck = false;
		}else {
			//next_token�� identifier�� �´ٸ� token_string�� ident1�� ����
			ident1 = token_string;
			lexical();
		}
		
		if(next_token != COLON) {
			// next_token�� colon�� �ƴ϶�� ���� ���ַ� �Ѿ�� �ʰ� ���(�����ڵ�: 10)
			errorCode = 10;
			if(next_token != IDENT) {
				//next_token�� identifier�� �ƴ϶�� ����ó��(�����ڵ�: 6)
				errorCode = 6;
				idCheck = false;

			}else {
				//next_token�� identifier�� �´ٸ� token_string�� ident2�� ����
				ident2 = token_string;
			}
		}else {
			lexical();
			if(next_token != IDENT) {
				//����ó��
				errorCode = 6;
				idCheck = false;
			}else {
				ident2 = token_string;
			}
		}
		if(!idCheck) return ERRORVALUE;
		// �޾ƿ� ���� b���� ����
		if(b) {
			if(symbolTable.containsKey(ident1))	{
				result = symbolTable.get(ident1);
				if(!symbolTable.containsKey(ident2)) {
					// ���� �ɺ����̺� ident2���� ���ٸ� ���ó��(�����ڵ�: 11)
					errorStr = ident2;
					errorCode = 11;
				}
			}
			else{
				// ���� �ɺ����̺� ident1���� ���ٸ� ���� ���� ���� ���� ��� ����ó��(�����ڵ�: 1) 
				errorStr = ident1;
				while(next_token != SEMI_COLON) lexical();
				errorCode = 1;
				return ERRORVALUE;
			}
			
		}else {
			if(symbolTable.containsKey(ident2))	{
				result = symbolTable.get(ident2);
				if(!symbolTable.containsKey(ident1)) {
					// ���� �ɺ����̺� ident1���� ���ٸ� ���ó��(�����ڵ�: 11)
					errorStr = ident1;
					errorCode = 11;
				}
			}
			else{
				// ���� �ɺ����̺� ident2���� ���ٸ� ���� ���� ���� ���� ��� ����ó��(�����ڵ�: 1) 
				errorStr = ident2;
				while(next_token != SEMI_COLON) lexical();
				errorCode = 1;
				return ERRORVALUE;
			}	
		}
		return result;
	}
	
	// ���� ����ó��
	static void SyntaxWarning(String str, int i) {
		switch(i) {
		case 1:
			System.out.println("<Error: ���ǵ��� ���� ����(" + str + ")�� ������>");
			break;
		case 2:
			System.out.println("<Warning: �ߺ� ������(" + str + ") ����>");
			break;
		case 3:
			System.out.println("<Warning: (" + str + ") �տ� ���� �ݷ��� �����ϴ�.>");
			break;
		case 4:
			System.out.println("<Warning: ��ȣ�� ���� �ʾҽ��ϴ�.>");
			break;
		case 5:
			System.out.println("<Warning: ? �����ڰ� �����ϴ�.>");
			break;
		case 6:
			System.out.println("<Error: ���׿����ڿ� identifier�� �����ֽ��ϴ�.>");
			break;
		case 7:
			System.out.println("<Error: (" + str + ") �տ� identifier�� �����ֽ��ϴ�.>");
			break;
		case 8:
			System.out.println("<Error: (=)�� �����ֽ��ϴ�.>");
			break;
		case 9:
			System.out.println("<Error: l-value ����.>");
			break;
		case 10:
			System.out.println("<Warning: :�� �����ϴ�.>");
			break;
		case 11:
			System.out.println("<Warning: ���ǵ��� ���� ����(" + str + ")�� ������>");
			break;
		default:
			System.out.println("<YES>");
			break;
				
		}
	}
	

	
	public static void main(String[] args) {

		// args�� ����� ���� �̸��� �ҷ��� ������ �о��
		try {
			fin = new FileReader(args[0]);
			getChar();
			// ���� ������ ������ ���̶�� ���� �б� ����
			do {
				lexical();
				// start �ɺ�
				parseStatements();
			}while(c != -1);
			// �ɺ����̺� �ִ� ������ ��� ���
			for (Map.Entry<String, Double> entry : symbolTable.entrySet()) {
				if(entry.getValue() == ERRORVALUE) {
					System.out.print(entry.getKey() + " : UNKNOWN; ");
				}else 	System.out.print(entry.getKey() + " : " + entry.getValue() + "; ");
	        }
			System.out.println();

			fin.close();
		}catch(IOException e) {
			System.out.println("����� ����");

		}
	}

}
