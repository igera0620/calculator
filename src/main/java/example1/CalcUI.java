package example1;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
    
public class CalcUI {
    public static void main(String[] args){
        MyFrame frame = new MyFrame("関数電卓");
        frame.setVisible(true);
    }

  // GUIのロジック
    static class MyFrame extends JFrame {
        private JTextField resultField; // 結果表示
        private JTextField exprField;   // 式表示
        private JTextField subField;

    MyFrame(String title){
        setTitle("関数電卓");
        setSize(600, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 5, 5, 5)); 
      // 6行×5列, ボタン間に5px隙間

        String[] buttons = {
            "2nd","π","e","C","⌫",
            "x²","¹/x","|x|","exp","mod",
            "²√x","(",")","n!","÷",
            "xʸ","7","8","9","×",
            "10ˣ","4","5","6","-",
            "log","1","2","3","+",
            "ln","+/-","0",".","="
        };

        for(String text : buttons){
            JButton btn = new JButton(text);
            btn.setFont(new Font("Arial", Font.PLAIN, 18));

            btn.setBackground(Color.DARK_GRAY);
            btn.setForeground(Color.WHITE);
            btn.setOpaque(true);
            btn.setBorderPainted(false);

            btn.addActionListener(e -> onButton(text)); // ボタン押下イベント
            panel.add(btn);
        }

      // 表示部
        resultField = new JTextField("0");  // 上段: 計算結果
        resultField.setHorizontalAlignment(JTextField.RIGHT); // 文字を右寄せに配置
        resultField.setFont(new Font("Arial", Font.BOLD, 28)); // 文字のフォントと大きさを指定
        resultField.setEditable(false); // 手入力できないように設定
        resultField.setPreferredSize(new Dimension(0, 60)); // Dimension(幅, 高さ)の指定が可能。0にすることにより、レイアウト任せになる。

        resultField.setBackground(Color.BLACK);
        resultField.setForeground(Color.WHITE);

        exprField = new JTextField("");     // 下段: 入力数式
        exprField.setHorizontalAlignment(JTextField.RIGHT);
        exprField.setFont(new Font("Arial", Font.BOLD, 28));
        exprField.setEditable(false);
        exprField.setPreferredSize(new Dimension(0, 40));

        exprField.setBackground(Color.BLACK);
        exprField.setForeground(Color.WHITE);

        subField = new JTextField("");
        subField.setHorizontalAlignment(JTextField.RIGHT);
        subField.setFont(new Font("Arial", Font.BOLD, 28));
        subField.setEditable(false);
        subField.setBackground(Color.BLACK);
        subField.setForeground(Color.WHITE);

        // 下段を左右に並べるパネル
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 0)); // ← 横2分割 1行2列に横並びで配置して、左右の間隔を10px開けて、上下の間隔なし
        bottomPanel.add(subField); // 左画面
        bottomPanel.add(exprField); // 右画面

        // 上段と下段を縦に配置するパネル
        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
        displayPanel.add(resultField); // 上
        displayPanel.add(bottomPanel); // 下（左右2分割）

        // 全体配置
        setLayout(new BorderLayout(5,5));
        add(displayPanel, BorderLayout.NORTH); // 上に表示部
        add(panel, BorderLayout.CENTER);        // 中央にボタン部
        }

    private double lastResult = 0.0; // 計算結果を保持する変数に初期値を設定

    // ボタン処理ロジック
    private void onButton(String text){
            adjustFontToFit(exprField);
            if(text.equals("C")){
                exprField.setText("");
                subField.setText("");
                resultField.setText("0");
                lastResult = 0;

                Font defaultFont = new Font("Arial", Font.BOLD, 28); // Cを押したらサイズ調整された文字が元に戻るように設定
                exprField.setFont(defaultFont);
                subField.setFont(defaultFont);
                resultField.setFont(defaultFont);
            } else if(text.equals("⌫")){
                String current = exprField.getText();
                if(current.length() > 0){
                    exprField.setText(current.substring(0, current.length()-1));
                }
            } else if(text.equals("=")){
                try {
                    String expr = exprField.getText();
                    // expr = CalcEngine.convertOperator(expr); // +-が重複してるときは、convertOperatorで変換
                    //exprFieldが空なら内部的にlastResultを初期値として扱うことで、答えに対しての計算が可能
                    if (expr.matches("^[+\\-×*/÷].*")) { 
                        expr = String.valueOf(lastResult) + expr;
                    }
                    double result = CalcEngine.evaluate(expr); // 計算ロジック呼び出し
                    DecimalFormat df;
                    String resultStr;
                    if (result == (long)result) {
                        df = new DecimalFormat("#,###");
                        resultStr = df.format((long)result);
                    } else {
                        df = new DecimalFormat("#,###.############");
                        resultStr = df.format(result);
                    }
                    if (String.valueOf(result).equals("0.0")) {
                        subField.setText(""); // 0.0だけ空欄
                    } else {
                        // lastResultが整数なら小数点なしで表示
                        String lastResultStr;
                        if (lastResult == (long)lastResult) {
                            lastResultStr = String.valueOf((long)lastResult);
                        } else {
                            lastResultStr = String.valueOf(lastResult);
                        }
                        // exprの先頭がlastResultの場合だけ置換
                        String displayExpr = expr.replaceFirst("^" + lastResult, lastResultStr)
                            .replaceAll("\\(0\\.0-", "(-")
                            .replaceAll("^0\\.0-", "-")
                            .replaceAll("0\\.0-", "-")
                            .replaceAll("^0-", "-");

                        int maxLengthSub = 20;
                        if (displayExpr.length() > maxLengthSub) {
                            displayExpr = displayExpr.substring(0, maxLengthSub); // 長い場合は省略
                        }
                        subField.setText(displayExpr);
                    } 
                    resultField.setText(resultStr); // 結果を上のディスプレイに表示
                    exprField.setText(""); // 右画面の式を削除
                    adjustFontToFit(resultField); // adjustFontTofitメソッドで自動調整してる
                    adjustFontToFit(subField); // 同上
                    lastResult = result; // 結果に対して計算が出来る様に、裏でデータが残るようにしてる
                    
                } catch (ArithmeticException e) {
                    resultField.setText("0で割ることは出来ません");
                } catch (Exception e) {
                    resultField.setText("Error");
                }
            // CalcEngineの方でπとeを変換しようとすると、expの計算をする際に不要な変換を行ってしまうため
            // UIに記述。windowsの関数電卓では3πと入力しても掛け算にはならないが、
            // 一般的な電卓では掛け算になるため下記を記述。
            } else if (
                text.equals("x²") || text.equals("¹/x") || text.equals("²√x") ||
                text.equals("exp") || text.equals("log") || text.equals("ln") ||
                text.equals("n!") || text.equals("+/-") ||
                text.equals("10ˣ") || text.equals("2nd")
            ) {
                try {
                    String expr = exprField.getText() + text;
                    double result = CalcEngine.evaluate(expr);
                    displayResult(result);
                } catch (ArithmeticException e) {
                    resultField.setText(e.getMessage());
                } catch (Exception e) {
                    resultField.setText("Error");
                }
            } else if (text.equals("π")) {
                try {
                    String current = exprField.getText();
                    String expr;
                    if (!current.isEmpty() && Character.isDigit(current.charAt(current.length() - 1))) { // 最後の1文字が数字かどうかを判断している
                        expr = current + "×" + Math.PI; 
                    } else {
                        expr = String.valueOf(Math.PI);
                    }
                    double result = CalcEngine.evaluate(expr);
                    displayResult(result);
                    subField.setText("");
                    exprField.setText("");
                    lastResult = result;
                } catch (Exception e) {
                    resultField.setText("Error");
                }
            } else if (text.equals("e")) {
                String current = exprField.getText();
                String expr;
                if (!current.isEmpty() && Character.isDigit(current.charAt(current.length() - 1))) {
                    expr = current + "×" + Math.E;
                } else {
                    expr = String.valueOf(Math.E);
                }
                    double result = CalcEngine.evaluate(expr);
                    displayResult(result);
                    subField.setText("");
                    exprField.setText("");
                    lastResult = result;
            } else {
                String current = exprField.getText(); // current変数にexprFieldの値を取得してる

                int maxLength = 12;
                if (current.length() >= maxLength) {
                    Toolkit.getDefaultToolkit().beep(); // ピッと音を鳴らす（任意）
                    return; // これ以上入力しない
                }
                // 演算子重複防止ロジック（このままでOK）
                if ("+-×÷".contains(text)) { // 複数条件
                    if (!current.isEmpty()) { // current変数の中身が空ではないが条件
                        char last = current.charAt(current.length() - 1); // char型のlast変数にcurrent変数の最後の値を代入してる
                        if ("+-×÷".indexOf(last) != -1) { // lastが演算子かどうかを判定
                            exprField.setText(current.substring(0, current.length() - 1) + text); // 演算子なら最後の文字（演算子）を削除して、その位置に新しく推した演算子（text）を追加して、exprFieldにセットしてる
                            return;
                        }
                    }
                    // current.isEmpty() の場合の分岐は不要
                }
                // ここをシンプルに
                exprField.setText(current + text);
            }
        }
        private void displayResult(double result) {
            DecimalFormat df;
            if (result == (long) result) {
                df = new DecimalFormat("#,###");
                resultField.setText(df.format((long) result)); // 整数なら小数点なしで表示
            } else {
                df = new DecimalFormat("#,###.############");
                resultField.setText(df.format(result)); // 小数なら小数点付きで表示
            }
        }

        private void adjustFontToFit(JTextField field) {
            Font fieldFont = field.getFont(); 
            String text = field.getText(); // 現在のフォントと表示中の文字列を取得

            if (text == null || text.isEmpty()) return; // 文字が空なら調整不要なので終了

            FontMetrics metrics = field.getFontMetrics(fieldFont); // このフォントで書くと何pxになるのかを教えてくれる
            int textWidth = metrics.stringWidth(text); // 今のフォントの文字列の横幅を(px)を計測
            int fieldWidth = field.getWidth(); // テキストフィールドの(px)を計測

            Insets insets = field.getInsets(); // JTextFieldの余白を取得
            int availableWidth = fieldWidth - insets.left - insets.right; // 実際に使える横幅

            if (availableWidth <= 0) return; // テキストフィールドがまだ用意されていなかったときには終了

            double ratio = (double) availableWidth / textWidth; // 実際の入れ物幅 / 文字幅
            int newFontSize = (int) (fieldFont.getSize() * Math.min(ratio, 1.0)); // 枠と文字の比率を使って、必要ならフォントを小さくする
            int minFontSize = 12; // 変数の宣言

            newFontSize = Math.max(minFontSize, newFontSize); // minFontSizeと比較して、newFontSizeが小さすぎる場合は下限値を適用する

            field.setFont(new Font(fieldFont.getName(), Font.BOLD, newFontSize));
        } // 現在のフォントと文字列を取る。そのフォントで書いた時のpxを測る。
          // フィールドの幅と比べてはみ出すならフォントを小さくする。
          // 最小サイズは12pxに張り付ける（小さくしすぎの防止）。
          // インセット（余白）も考慮して、実際の表示領域で調整する。
    }
}
