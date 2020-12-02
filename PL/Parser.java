import java.io.*;
import java.util.Map;
import java.util.TreeMap;

public class Parser {
	static Map<String, Double> symbolTable = new TreeMap<String, Double>();
	static FileReader fin;
	// 문장에 포함된 식별자, 정수, 연산자 갯수
	static int id = 0;
	static int constant = 0;
	static int operand = 0;
	//예외처리 변수
	static String errorStr = null;
	static int errorCode;
	private static final double ERRORVALUE = Double.MAX_VALUE;
	//lexeme 변수 및 토큰변수
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
	
	 // 어휘 분석문
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
			// 식별자 갯수
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
			// 상수 갯수
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
			// 중복연산자 및 == 기호 처리
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
			// 연산자 갯수
			if(next_token == ADD_OP | next_token == SUB_OP | next_token == MULT_OP | next_token == DIV_OP | next_token == QUESTION_OP) operand++;
			break;
		case UNKNOWN:
			getChar();
			break;
		}
		
		token_string = new String(lexeme, 0, lexLen);
		System.out.print(token_string + " ");
	}
	
	// 문자 배열에 읽어온 문자 저장
	static void addChar() {
		if(lexLen <= 98) {
			lexeme[lexLen] = (char)c;
			lexLen++;
		}else {
			System.out.println("Error - lexeme is too long");
		}
	}
	
	// 파일에서 문자를 읽어옴
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
			System.out.println("입출력 오류");

		}
	}
	
	// 공백 제거
	static void getNonBlank() {
		while(c <= 32) getChar();
	}
	
	// 연산자의 토큰 확인
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
	
	//<statements> → <statement> | <statement> <semi_colon> <statements>
	static void parseStatements() {
		// identifier 저장
		String ident = token_string;
		
		double result = parseStatement();
		
		// 문장의 끝 -> semicolon 이라면 심볼테이블에 연산한 것을 저장 후 출력
		if(next_token == SEMI_COLON) {
			// l-value 에러 예외 처리
			if(errorCode != 9) symbolTable.put(ident, result);
			//심볼테이블 저장 및 출력
			System.out.print(" ==> ID : " + id + "; CONST : " + constant + "; OP : " + operand + " ");
			SyntaxWarning(errorStr, errorCode);
			
			// 예외처리 변수 및 갯수 초기화
			id = 0;
			operand = 0;
			constant = 0;
			errorCode = 0;
		}else {
			// semicolon이 아니라면 에러코드 3 저장 및 심볼테이블에 결과 저장
			errorStr = token_string;
			errorCode = 3;
			symbolTable.put(ident, result);
			
			// identifier 저장 후 다시 파싱
			ident = token_string;
			result = parseStatement();
			symbolTable.put(ident, result);
			//심볼테이블 저장 및 출력
			System.out.print(" ==> ID : " + id + "; CONST : " + constant + "; OP : " + operand + " ");
			SyntaxWarning(errorStr, errorCode);
			
			id = 0;
			operand = 0;
			constant = 0;
			errorCode = 0;
		}
		
	}
	
	// <statement>	→ <ident> <assignment_operator> <expression>
	static double parseStatement() {
		double result = ERRORVALUE;
		if(next_token == IDENT) {
		}else {
			// l-value 오류 예외처리(에러코드: 9)
			errorCode = 9;
			while(next_token != SEMI_COLON) lexical();
			return ERRORVALUE;
		}
		lexical();
		if(next_token == ASSIGN_OP) {
		}else {
			// =이 없으면 문장 끝으로 이동 후 UNKNOWN 저장(에러코드: 8)
			errorCode = 8;
			while(next_token != SEMI_COLON) lexical();
			return ERRORVALUE;
		}
		// 정상적인 어휘 순서면 다음 Expression으로 파싱
		result = parseExpression();
		return result;
	}
	// <expression>	→ <term> <plus_operator> <expression> | <term> <minus_operator> <expression> | <term>
	static double parseExpression() {
		// Term으로 파싱
		double result = parseTerm();
		
		// next_token에 +연산자 및 -연산자일 경우 계속 계산
		while(next_token == ADD_OP | next_token == SUB_OP) {
			if(next_token == ADD_OP) {
				// 아래 단계에서 받아온 result 값이 ERRORVALUE면 계산 하지 않음
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
	//<term>	→ <factor> <star_operator> <term> | <factor> <slash_operator> <term> | <factor>
	static double parseTerm() {
		// Factor로 파싱
		double result = parseFactor();
		
		// next_token에 *연산자 및 /연산자일 경우 계속 계산
		while(next_token == MULT_OP | next_token == DIV_OP) {
			if(next_token == MULT_OP) {
				// 아래 단계에서 받아온 result 값이 ERRORVALUE면 계산 하지 않음
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
	
	// <factor>	→ <left_parenthesis> <expression> <right_parenthesis> | <ident> | <constant> | <ident><condition><ident><question_operator><compare_value>
	static double parseFactor() {
		double result = ERRORVALUE;
		
		String ident = null;
		lexical();
		
		// next_token이 identifier일 경우 
		if(next_token == IDENT) {
			ident = token_string;
			// 심볼테이블에서 값을 받아와 result에 저장
			if(symbolTable.containsKey(token_string)) {
				result = symbolTable.get(token_string);
			}else {
				// 심볼테이블에 없다면 정의되지 않은 변수가 참조된 것으로 예외 처리(에러코드: 1)
				errorStr = token_string;
				errorCode = 1;
				// result값을 ERRORVALUE로 저장 
				result = ERRORVALUE;
			}
			lexical();
			// next_token이 상수일 경우
		}else if(next_token == CONSTANT) {
			try {
				// 그 값을 result에 저장
				result = Double.parseDouble(token_string);
				//NumberFormatException으로 double변수 파싱에 예외 처리
			}catch(NumberFormatException e) {
				System.out.println("error");
			}
			lexical();
			
			// next_token이 왼쪽 괄호일 경우
		}else if(next_token == LEFT_PAREN) {
			// 다시 Expression으로 파싱
			result = parseExpression();
			// 정상적으로 오른쪽 괄호인 경우 어휘분석
			if(next_token == RIGHT_PAREN) {
				lexical();
			}else {
				// 오른쪽 괄호가 없는 것으로 예외 처리(에러코드: 4)
				errorCode = 4;
				
			}

		}
		
		// next_token이 비교 연산자일 경우 condition으로 파싱
		if(next_token == LESS_KEYWORD | next_token == GREATER_KEYWORD | next_token == EQUAL_KEYWORD) {
			result = parseCondition(ident);
		}
		
		return result;
	}
	// <condition> 	→ <less_keyword> | <greater_keyword> | <equal_keyword>
	static double parseCondition(String str) {
		double result;
		
		if(next_token == LESS_KEYWORD) {
			lexical();
			// < 다음에 온 것이 identifier가 아니라면 예외처리
			if(next_token != IDENT) {
				// 문장 끝까지 출력 후 ERRORVALUE 리턴(에러코드: 7)
				errorStr = token_string;
				while(next_token != SEMI_COLON) lexical();
				errorCode = 7;
				return ERRORVALUE;
			}
			// 다음 CompartValue로 파싱
			if(symbolTable.containsKey(token_string))	result = parseCompareValue(symbolTable.get(str) < symbolTable.get(token_string));
			else {
				// 만약 심볼테이블에 token_string값이 없다면 정의 되지 않은 변수 사용 예외처리(에러코드: 1) 
				errorStr = token_string;
				while(next_token != SEMI_COLON) lexical();
				errorCode = 1;
				return ERRORVALUE;
			}
		}else if(next_token == GREATER_KEYWORD) {
			lexical();
			if(next_token != IDENT) {
				//예외처리
				errorStr = token_string;

				while(next_token != SEMI_COLON) lexical();
				errorCode = 7;
				return ERRORVALUE;
			}
			// 다음 CompareValue로 파싱
			if(symbolTable.containsKey(token_string))	result = parseCompareValue(symbolTable.get(str) > symbolTable.get(token_string));
			else {
				// 만약 심볼테이블에 token_string값이 없다면 정의 되지 않은 변수 사용 예외처리(에러코드: 1) 
				errorStr = token_string;
				while(next_token != SEMI_COLON) lexical();
				errorCode = 1;
				return ERRORVALUE;
			}
		}else {
			lexical();
			if(next_token != IDENT) {
				//예외처리
				errorStr = token_string;
				while(next_token != SEMI_COLON) lexical();
				errorCode = 7;
				return ERRORVALUE;
			}

			// 다음 CompartValue로 파싱
			if(symbolTable.containsKey(token_string))	result = parseCompareValue(symbolTable.get(str) == symbolTable.get(token_string));
			else {
				// 만약 심볼테이블에 token_string값이 없다면 정의 되지 않은 변수 사용 예외처리(에러코드: 1) 
				errorStr = token_string;
				while(next_token != SEMI_COLON) lexical();
				errorCode = 1;
				return ERRORVALUE;
			}
		}
		// next_token이 semicolon이면 어휘분석을 하지 않음
		if(next_token != SEMI_COLON)	lexical();
		return result;
	}
	// <compare_value> → <ident> <colon> <ident>(ident : ident이기 때문에 상수를 취급하지 않음)
	static double parseCompareValue(boolean b) {
		double result = ERRORVALUE;
		String ident1 = null;
		String ident2 = null;
		// identifier가 하나 없다면 false
		boolean idCheck = true;
		lexical();
		// 삼항연산에서 ?가 없다면 
		if(next_token != QUESTION_OP) {
			// 다음 어휘로 넘어가지 않고 경고(에러코드: 5)
			errorCode = 5;
		}else {
			lexical();
		}
		
		if(next_token != IDENT) {
			//next_token이 identifier가 아니라면 예외처리(에러코드: 6)
			errorCode = 6;
			// 그리고 idCheck을 false로 바꿔줌
			idCheck = false;
		}else {
			//next_token이 identifier가 맞다면 token_string을 ident1에 저장
			ident1 = token_string;
			lexical();
		}
		
		if(next_token != COLON) {
			// next_token이 colon이 아니라면 다음 어휘로 넘어가지 않고 경고(에러코드: 10)
			errorCode = 10;
			if(next_token != IDENT) {
				//next_token이 identifier가 아니라면 예외처리(에러코드: 6)
				errorCode = 6;
				idCheck = false;

			}else {
				//next_token이 identifier가 맞다면 token_string을 ident2에 저장
				ident2 = token_string;
			}
		}else {
			lexical();
			if(next_token != IDENT) {
				//예외처리
				errorCode = 6;
				idCheck = false;
			}else {
				ident2 = token_string;
			}
		}
		if(!idCheck) return ERRORVALUE;
		// 받아온 값이 b값에 따라
		if(b) {
			if(symbolTable.containsKey(ident1))	{
				result = symbolTable.get(ident1);
				if(!symbolTable.containsKey(ident2)) {
					// 만약 심볼테이블에 ident2값이 없다면 경고처리(에러코드: 11)
					errorStr = ident2;
					errorCode = 11;
				}
			}
			else{
				// 만약 심볼테이블에 ident1값이 없다면 정의 되지 않은 변수 사용 예외처리(에러코드: 1) 
				errorStr = ident1;
				while(next_token != SEMI_COLON) lexical();
				errorCode = 1;
				return ERRORVALUE;
			}
			
		}else {
			if(symbolTable.containsKey(ident2))	{
				result = symbolTable.get(ident2);
				if(!symbolTable.containsKey(ident1)) {
					// 만약 심볼테이블에 ident1값이 없다면 경고처리(에러코드: 11)
					errorStr = ident1;
					errorCode = 11;
				}
			}
			else{
				// 만약 심볼테이블에 ident2값이 없다면 정의 되지 않은 변수 사용 예외처리(에러코드: 1) 
				errorStr = ident2;
				while(next_token != SEMI_COLON) lexical();
				errorCode = 1;
				return ERRORVALUE;
			}	
		}
		return result;
	}
	
	// 구문 예외처리
	static void SyntaxWarning(String str, int i) {
		switch(i) {
		case 1:
			System.out.println("<Error: 정의되지 않은 변수(" + str + ")가 참조됨>");
			break;
		case 2:
			System.out.println("<Warning: 중복 연산자(" + str + ") 제거>");
			break;
		case 3:
			System.out.println("<Warning: (" + str + ") 앞에 세미 콜론이 없습니다.>");
			break;
		case 4:
			System.out.println("<Warning: 괄호를 닫지 않았습니다.>");
			break;
		case 5:
			System.out.println("<Warning: ? 연산자가 없습니다.>");
			break;
		case 6:
			System.out.println("<Error: 삼항연산자에 identifier가 빠져있습니다.>");
			break;
		case 7:
			System.out.println("<Error: (" + str + ") 앞에 identifier가 빠져있습니다.>");
			break;
		case 8:
			System.out.println("<Error: (=)이 빠져있습니다.>");
			break;
		case 9:
			System.out.println("<Error: l-value 오류.>");
			break;
		case 10:
			System.out.println("<Warning: :가 없습니다.>");
			break;
		case 11:
			System.out.println("<Warning: 정의되지 않은 변수(" + str + ")가 참조됨>");
			break;
		default:
			System.out.println("<YES>");
			break;
				
		}
	}
	

	
	public static void main(String[] args) {

		// args에 저장된 파일 이름을 불러와 파일을 읽어옴
		try {
			fin = new FileReader(args[0]);
			getChar();
			// 읽은 파일이 문장의 끝이라면 파일 읽기 종료
			do {
				lexical();
				// start 심볼
				parseStatements();
			}while(c != -1);
			// 심볼테이블에 있는 값들을 모두 출력
			for (Map.Entry<String, Double> entry : symbolTable.entrySet()) {
				if(entry.getValue() == ERRORVALUE) {
					System.out.print(entry.getKey() + " : UNKNOWN; ");
				}else 	System.out.print(entry.getKey() + " : " + entry.getValue() + "; ");
	        }
			System.out.println();

			fin.close();
		}catch(IOException e) {
			System.out.println("입출력 오류");

		}
	}

}
