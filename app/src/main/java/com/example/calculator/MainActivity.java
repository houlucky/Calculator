package com.example.calculator;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
public class MainActivity extends Activity {

    EditText editText;
    String str1="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initEditText();
        setNumButtonListener();
        setOperationButtonListener();
        setBackButtonListener();
        setClearEditView();
        setResultButton();
    }
    public void setBackButtonListener(){
        Button backButton = (Button)findViewById(R.id.buttonBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteOneChar();
                int cursorIndex = editText.getSelectionStart();
                editText.setText(str1);
                editText.setSelection(cursorIndex);
            }
        });
    }


    public  void deleteOneChar(){
        Editable editable = editText.getText();
        if(!str1.isEmpty() && editText.getSelectionStart() >=1)
            editable.delete(editText.getSelectionStart() - 1, editText.getSelectionStart());
        str1 = editable.toString();
    }

    public void initEditText(){
        editText = (EditText)findViewById(R.id.text_view);
    }

    public void setNumButtonListener(){
        int[] numButtons = new int[] {R.id.button00, R.id.button01, R.id.button02, R.id.button03,
                R.id.button04, R.id.button05, R.id.button06, R.id.button07,
                R.id.button08, R.id.button09};
         Log.d("temp1", str1);
        for(int i=0; i< numButtons.length; i++){//为每一个数字按钮设置一个监听器
            Button temp = (Button)findViewById(numButtons[i]);
            temp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    insertChar(v);
                }
            });
        }
    }

    public void setOperationButtonListener(){
        int[] operationButtons = new int[]{R.id.buttonAdd, R.id.buttonMultiply, R.id.buttonDivide, R.id.buttonSubtract,R.id.buttonPoint};
        for(int i=0; i< operationButtons.length; i++){//为每一个运算按钮设置一个监听器
            Button temp1 = (Button)findViewById(operationButtons[i]);
            temp1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!str1.isEmpty()){
                        String lastChar = str1.substring(str1.length()-1, str1.length());
                        if(!isNum(lastChar))
                            deleteOneChar();
                    }
                    Log.d("data", String.valueOf(editText.getSelectionStart()));
                    insertChar(v);
                }
            });
        }
    }

    public void insertChar(View v){
        int cursorIndex;
        str1 = editText.getText().insert(editText.getSelectionStart(), String.valueOf(((Button)v).getText())).toString();
        cursorIndex = editText.getSelectionStart();
        editText.setText(str1);
        editText.setTextIsSelectable(true);
        editText.setSelection(cursorIndex);
    }

    public void setClearEditView(){
        Button buttonC  = (Button)findViewById(R.id.buttonDelete);
        buttonC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str1 = "";
                editText.setText(str1);
            }
        });
    }

    public void setResultButton(){
        Button buttonResult = (Button)findViewById(R.id.buttonResult);
        buttonResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("temp1", str1);
                if(!"".equals(str1) ){
                    if(computeWithStack(str1) % 1.0 == 0)//如果最后结果是一个浮点型整数(333.0)，我希望我得到的是一个整形整数(333)
                        str1 = String.valueOf((long) computeWithStack(str1));
                    else
                        str1 = String.valueOf(computeWithStack(str1));
                }
                Log.d("temp1", str1);
                editText.setText(str1);
                editText.setSelection(str1.length());
            }
        });
    }

    public double computeWithStack(String computeExpr) {
        StringTokenizer stringTokenizer = new StringTokenizer(computeExpr, "+-×÷", true);
        Stack<Double> numStack = new Stack<Double>();
        Stack<Operator> operatorStack = new Stack<Operator>();
        Map<String, Operator> operatorMap = this.geOperatorMap();
        String currentEle;
        while (stringTokenizer.hasMoreTokens()) {
            currentEle = stringTokenizer.nextToken();
            if(numStack.isEmpty() && operatorStack.isEmpty()){//两个栈都是空的，说明是第一个元素准备入栈
                if(!this.isNum(currentEle))
                    numStack.push(0.0);
            }
            if (this.isNum(currentEle)) {
                numStack.push(Double.parseDouble(currentEle));
            } else {
                Operator currentOper = operatorMap.get(currentEle);
                if (currentOper != null) { //没有这个运算符则说明是()括号
                    while (!operatorStack.isEmpty() && operatorStack.peek().priority() >= currentOper.priority()) {
                        compute(numStack, operatorStack);
                    }
                    operatorStack.push(currentOper);
                } else {//括号的处理！！！

                }
            }
        }
        if ("".equals(computeExpr)) {
            return 0;
        }
        if(compareStack(numStack,operatorStack)){//true-数目相同 false-数目不相同判断数据栈中元素的个数是否比operatorStack栈中的元素多一个
            operatorStack.pop();
        }
        while (!operatorStack.isEmpty()) {
            compute(numStack,operatorStack);
        }

        return numStack.pop();
    }

    public boolean compareStack(Stack<Double> numStack, Stack<Operator> operatorStack){
        int i=0,j=0;
        for(double A : numStack){
            Log.d("temp", String.valueOf(A));
            i++;
        }
        for (Operator B : operatorStack){
           Log.d("temp", String.valueOf(B));
            j++;
        }

        return i==j;
    }

    public void compute(Stack<Double> numStack, Stack<Operator> operatorStack){
        double num2 = numStack.pop();
        double num1 = numStack.pop();
        double result = operatorStack.pop().compute(num1, num2);
        numStack.push(result);
    }

    public boolean isNum(String s){
        String numRegex =  "^\\d+(\\.\\d+)?$";
        return Pattern.matches(numRegex, s);
    }

    public Map<String,Operator> geOperatorMap(){
        return  new HashMap<String,Operator>(){
            private static final long serialVersionUID = 7706718608122369958L;
            {
                put("+", Operator.PLUS);
                put("-", Operator.MINUS);
                put("×", Operator.MULTIPLY);
                put("÷", Operator.DIVIDE);
            }
        };
    }

    public enum Operator{

        PLUS{
            @Override
            public double compute(double num1, double num2) {
                return num1+num2;
            }

            @Override
            public int priority() {
                return 1;
            }
        },

        MINUS{
            @Override
            public double compute(double num1, double num2) {
                return num1-num2;
            }

            @Override
            public int priority() {
                return 1;
            }
        },

        MULTIPLY{
            @Override
            public double compute(double num1, double num2) {
                return num1*num2;
            }

            @Override
            public int priority() {
                return 2;
            }
        },

        DIVIDE{
            @Override
            public double compute(double num1, double num2) {
                return num1/num2;
            }

            @Override
            public int priority() {
                return 2;
            }
        };

        public abstract double compute(double num1, double num2);
        public abstract int priority();
    }
}





