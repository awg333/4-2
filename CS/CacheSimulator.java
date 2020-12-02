import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class CacheSimulator {
	
	// Counting hit & miss during cache simulating 
	static int totalHit = 0;
	static int totalMiss = 0;
	
	// A Block class stores a tag and validBit of block and LRU for replacement
	public class Block{
		int tag;
		int LRU;
		boolean validBit;
		
		// initialize
		public Block() {
			tag = Integer.MIN_VALUE;
			LRU = Integer.MIN_VALUE;
			validBit = false;
		}
	}
	
	// A Set class that has an array of Line 
	public class Set{
		Block[] blocks;
		// initializing arrays of block  
		public Set(int n) {
			blocks = new Block[n];
			for(int i = 0; i < n; i++) {
				blocks[i] = new Block();
			}
			
		}
	}
	
	// A Cache class that has an array of Set
	public class Cache{
		Set[] sets;
		// initializing arrays of set
		public Cache(int s, int n) {
			sets = new Set[s];
			for(int i = 0; i < s; i++) {
				sets[i] = new Set(n);
			}
		}
	}
	
	// A method that verifies if parameter is a power of 2 or 1 
	public static boolean isPowerOfTwo(int num) {
        if(num<=0) return false;
        if(num==1) return true;
        
        int nResult = num & (num-1);

    	if( nResult == 0 )
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// A Scanner class for user input
		Scanner scanner = new Scanner(System.in);
		// Get user input and Store it in inputCommand
		String inputCommand = scanner.nextLine();
		// Tokenize the string by white-space after input
		StringTokenizer st = new StringTokenizer(inputCommand, " ");
		// String for a file name 
		String inputFile = null;
		// integers for input 
		int s = 0, n = 0, m = 0;
		scanner.close();
		boolean correctCommand = false;
		
		while(st.hasMoreTokens()) {
			String temp = st.nextToken();
			
			try {
				switch(temp) {
				case "cache_simulator":
					correctCommand = true;
					break;
				case "-s":
					// If temp is '-s', get next token and store it in s
					String S = new String(st.nextToken());
					s = Integer.parseInt(S);
					if(isPowerOfTwo(s)) {
						correctCommand = true;
					}else {
						correctCommand = false;
					}
					break;
				case "-n":
					// If temp is '-n', get next token and store it in n
					String N = new String(st.nextToken());
					n = Integer.parseInt(N);
					if(isPowerOfTwo(n)) {
						correctCommand = true;
					}else {
						correctCommand = false;
					}
					break;
				case "-m":
					// If temp is '-m', get next token and store it in m
					String M = new String(st.nextToken());
					m = Integer.parseInt(M);
					if(isPowerOfTwo(m)) {
						correctCommand = true;
					}else {
						correctCommand = false;
					}
					break;
				default: 
					// store temp in inputFile
					inputFile = temp;
						
				}
			}catch(NumberFormatException e) {
				System.out.println(temp + "다음에 정수를 입력하세요.");
			}
			if(!correctCommand) {
				break;
			}
		}
		if(!correctCommand) {
			System.out.println("올바른 커맨드를 입력하세요.(-s, -n, -m의 숫자는 1 또는 2의 거듭제곱을 입력해야 합니다.)");

		}else {
			try {
				// BufferedReader, BufferedWriter for writing and reading file
				BufferedReader br = new BufferedReader(new FileReader(new File(inputFile + ".txt")));
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File("test.out")));
				// a line of the file
				String line;
				// integer for replacement
				int lru = 0;
				// create Cache class with input size
				Cache cache = new CacheSimulator().new Cache(s, n);
			    while ((line = br.readLine()) != null) {
			    	// boolean for replacement, hit, miss
			    	boolean hitCheck = false;
			    	boolean missCheck = false;
			    	boolean replacement = true;
			    	
			    	// Convert received hexadecimal string to integer(without "0x")
			    	long lineAdd = Long.parseLong(line.substring(2), 16);
			    	// Convert integer to binary string
			    	String tempAdd = Long.toBinaryString(lineAdd);
			    	String byteAdd;
			    	// set binary string to length 32 for the following process 
			    	if(tempAdd.length() < 32) {
			    		char[] c = new char[32 - tempAdd.length()];
			    		for(int i = 0; i < c.length; i++) {
			    			c[i] = '0';
			    		}
			    		String tempChar = new String(c);
			    		byteAdd = tempChar + tempAdd;
			    		
			    	}else {
			    		byteAdd = tempAdd;
			    	}
			    	// write the line in file
			    	bw.write(line + "\t");
			    	
			    	// log2 m and s for address mapping
			    	int logm = (int)(Math.log10(m)/Math.log10(2));
			    	int logs = (int)(Math.log10(s)/Math.log10(2));
			    	
			    	// calculate a block Address
			    	String blockAdd = byteAdd.substring(0, byteAdd.length() - 2 - logm);
			    	
			    	// calculate a Set Index
			    	String setIndex;
			    	if(s == 1) {
			    		setIndex = "0";
	
			    	}else {
			    		setIndex = blockAdd.substring(blockAdd.length() - logs, blockAdd.length());
	
			    	}
			    	// calculate a tag
			    	String tag = blockAdd.substring(0, blockAdd.length() - logs);
			    	
			    	// Convert binary of SetIndex to integer
			    	int index = Integer.parseInt(setIndex, 2);
			    	
			    	// for loop as many blocks in a set
			    	for(int i = 0; i < n; i++) {
			    		// if a validBit in current cache is false 
			    		if(cache.sets[index].blocks[i].validBit == false) {
			    			// store the tag, change validBit is true, store lru for replacement in current cache
			    			// then change misscheck is true, replacement is false
			    			missCheck = true;
			    			cache.sets[index].blocks[i].tag = Integer.parseInt(tag, 2);
			    			cache.sets[index].blocks[i].LRU = lru;
			    			cache.sets[index].blocks[i].validBit = true;
			    			replacement = false;
			    			break;
			    		}else {
			    			// if a validBit in current cache is true, compare the current tag with the tag in current cache.
			    			if(cache.sets[index].blocks[i].tag == Integer.parseInt(tag, 2)) {
			    				// if these are same,  store lru for replacement in current cache
				    			// then change hitcheck is true, replacement is false
			    				hitCheck = true;
			    				replacement = false;
			    				cache.sets[index].blocks[i].LRU = lru;
			    				break;
			    			}else {
			    				// if these are different, change misscheck is true, replacement is true
				    			missCheck = true;
			    				replacement = true;
	
			    			}
			    		}
			    	}
			    	// if replacement is true
			    	if(replacement) {
			    		// store tag at the lowest LRU
			    		int tmp = 0;
			    		if(n == 1) {
			    			cache.sets[index].blocks[0].tag = Integer.parseInt(tag, 2);
			    			cache.sets[index].blocks[0].LRU = lru;
			    		}else {
			    			for(int i = 0; i < n-1; i++) {
			    				if(cache.sets[index].blocks[i].LRU < cache.sets[index].blocks[i+1].LRU) {
			    					tmp = i;
			    				}else {
			    					tmp = i+1;
			    				}
			    			}
			    			cache.sets[index].blocks[tmp].tag = Integer.parseInt(tag, 2);
			    			cache.sets[index].blocks[tmp].LRU = lru;
			    		}
			    	}
			    	
			    	// if hitCheck is true, write "hit" in file 
			    	if(hitCheck) {
			    		totalHit++;
				    	bw.write("Hit" + "\t");
				    	// if missCheck is true, write "miss" in file 
			    	}else if(missCheck) {
			    		totalMiss++;
				    	bw.write("Miss" + "\t");
	
			    	}
			    	// check replacement
			    	if(replacement) {
				    	bw.write("Replacement : o" + "\n");
	
			    	}else {
				    	bw.write("Replacement : x" + "\n");
	
			    	}
			    	// increase 1 lru for the replacement policy 'LRU'
			    	lru++;
			    	
			    }
			    br.close();
			    // write total hit and miss in file
			    bw.write("# of cache hits\t" + totalHit + "\n" );
			    bw.write("# of cache misses\t" + totalMiss + "\n" );
	
			    bw.close();
			}catch(IOException e) {
				System.out.println("파일읽기를 실패하였습니다.");
			}catch(NumberFormatException e) {
				System.out.println("error");
			}
		
		}
		
	}

}
