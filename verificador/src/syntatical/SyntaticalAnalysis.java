/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syntatical;

import lexical.LexicalAnalysis;
import lexical.TokenType;
import lexical.Lexeme;
import semantical.Letter;
import semantical.Transition;
import semantical.State;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static semantical.State.setFinal;
import static semantical.State.setInicial;
import static lexical.LexicalAnalysis.line;
import static lexical.LexicalAnalysis.st;
import static semantical.State.isFinal;
import static semantical.State.isInicial;
import static verificador.Verificador.S;
import static verificador.Verificador.L;
import static verificador.Verificador.T;
import static verificador.Verificador.I;
import static verificador.Verificador.F;

/**
 *
 * @author silveira
 */
public class SyntaticalAnalysis {
    private Lexeme current;
    private LexicalAnalysis lex;
    
    private boolean endParsing = false;
    
    public SyntaticalAnalysis(LexicalAnalysis lex) throws IOException{
        try {
            this.lex = lex;
            this.current = lex.nextToken();
        } catch(IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
    
    private void matchToken(TokenType type) throws IOException{
        try {
            if(type==current.type){
                if(endParsing==false)
                    current = lex.nextToken();
            }
            else {
                if(current.type==TokenType.END_OF_FILE) {
                    throw new IOException(line+": Unexpected end of file");
                }
                else if(current.type!=TokenType.END_OF_FILE && current.type!=TokenType.UNEXPECTED_EOF && current.type!=TokenType.INVALID_TOKEN){
                    if(current.type==TokenType.STRING) {
                        throw new IOException(line+": Unexpected lexeme ["+current.token+"]");
                    }
                    else {
                        throw new IOException(line+": Unexpected lexeme ["+st.getSymbol(current.type)+"]");
                    } 
                }
            }
        } catch(IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        
    }
    
    // <af> ::= '{' "af" ':' '[' <states> ',' <letters> ',' <transitions> ',' <inicials> ',' <finals> ']' '}' 
    public void procAf() throws IOException {
        matchToken(TokenType.CBRA_OPEN);
        matchToken(TokenType.STRING);
        matchToken(TokenType.COLON);
        matchToken(TokenType.SBRA_OPEN);
        S = procStates();
        matchToken(TokenType.COMMA);
        L = procLetters();
        matchToken(TokenType.COMMA);
        T = procTransitions();
        matchToken(TokenType.COMMA);
        I = procInicials();
        matchToken(TokenType.COMMA);
        F = procFinals();
        matchToken(TokenType.SBRA_CLOSE);
        endParsing = true;
        matchToken(TokenType.CBRA_CLOSE);
    }
    
    //<states> ::= '[' <state> {, <state> } ']'
    private ArrayList<State> procStates() throws IOException {
        try {
            matchToken(TokenType.SBRA_OPEN);
            State s = procState();
            S.add(s);
            while(this.current.type==TokenType.COMMA) {
                matchToken(TokenType.COMMA);
                s = procState();
                S.add(s);
            }
            if(this.current.type==TokenType.SBRA_CLOSE) {
                matchToken(TokenType.SBRA_CLOSE);
            }
        } catch(IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return S;
    }
    
    private State procState() throws IOException {
        try {
            if(this.current.type==TokenType.STRING) {
                if(current.token.length()==0 || current.token.equals(" ")) {
                    throw new IOException(line+": Unexpected lexeme ["+current.token+"]");
                }
                State s = new State(current.token);
                matchToken(TokenType.STRING);
                return s;
            }
        } catch(IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return null;
    }
    
    //<letters> ::= '[' <letter> {, <letter> } ']'
    private ArrayList<Letter> procLetters() throws IOException {
        try {
            matchToken(TokenType.SBRA_OPEN);
            Letter l = procLetter();
            L.add(l);
            while(this.current.type==TokenType.COMMA) {
                matchToken(TokenType.COMMA);
                l = procLetter();
                L.add(l);
            }
            if(this.current.type==TokenType.SBRA_CLOSE) {
                matchToken(TokenType.SBRA_CLOSE);
                return L;
            }
        } catch(IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return null;
    }
   
    private Letter procLetter() throws IOException {
        try {
            if(this.current.type==TokenType.STRING) {
                if(current.token.length()!=1) {
                    throw new IOException(line+": Unexpected lexeme ["+current.token+"]");
                }
                Letter l = new Letter(current.token.charAt(0));
                matchToken(TokenType.STRING);
                return l;
            }
        } catch(IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return null;
    }
    
    //<transitions> ::= '[' <state> ',' <letter> ',' <state> ']' {, '[' <state> ',' <letter> ',' <state> ']'}
    private ArrayList<Transition> procTransitions() throws IOException {
        try {
            matchToken(TokenType.SBRA_OPEN);
            matchToken(TokenType.SBRA_OPEN);
            State from = procState();
            matchToken(TokenType.COMMA);
            Letter letter = procLetter();
            matchToken(TokenType.COMMA);
            State to = procState();
            Transition t = new Transition(from, letter, to);
            T.add(t);
            matchToken(TokenType.SBRA_CLOSE);
            while(this.current.type==TokenType.COMMA) {
                matchToken(TokenType.COMMA);
                matchToken(TokenType.SBRA_OPEN);
                from = procState();
                matchToken(TokenType.COMMA);
                letter = procLetter();
                matchToken(TokenType.COMMA);
                to = procState();
                t = new Transition(from, letter, to);
                T.add(t);
                matchToken(TokenType.SBRA_CLOSE);
            }
            if(this.current.type==TokenType.SBRA_CLOSE) {
                matchToken(TokenType.SBRA_CLOSE);
                return T;
            }
        } catch(IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return null;
    }
    
    //<inicials> ::= '['<state> {, <state> '} ']'
    private ArrayList<State> procInicials() throws IOException {
        try {
            matchToken(TokenType.SBRA_OPEN);
            State s = procState();
            setInicial(s);
            while(this.current.type==TokenType.COMMA) {
                matchToken(TokenType.COMMA);
                s = procState();
                setInicial(s);
            }
            if(this.current.type==TokenType.SBRA_CLOSE) {
                matchToken(TokenType.SBRA_CLOSE);
                return S;
            }
        } catch(IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return null;
    }
    
    //<finals> ::= '[' <state> {, <state> } ']'
    private ArrayList<State> procFinals() throws IOException  {
        try {
            matchToken(TokenType.SBRA_OPEN);
            State s = procState();
            setFinal(s);
            while(this.current.type==TokenType.COMMA) {
                matchToken(TokenType.COMMA);
                s = procState();
                setFinal(s);
            }
            if(this.current.type==TokenType.SBRA_CLOSE) {
                matchToken(TokenType.SBRA_CLOSE);
                return S;
            }
        } catch(IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return null;
    }
}
