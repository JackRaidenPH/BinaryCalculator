package dev.jackraidenph.binarycalculator;

import dev.jackraidenph.binarycalculator.utility.ParsingUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class Controller {
    @FXML
    private Button evaluateButton;
    @FXML
    private TextArea expressionArea;
    @FXML
    private Label answerLabel;

    @FXML
    protected void onEvaluate() {
        String toEvaluate = expressionArea.getText();
        String result = ParsingUtils.parsePostfix(ParsingUtils.infixToPostfix(toEvaluate));
        answerLabel.setText(answerLabel.getText() + toEvaluate + " = " + ParsingUtils.floatFivePrecisionFormat(Float.valueOf(result)) + (result.contains(".") ? " FLOAT" : " INTEGER") + "\n");
    }
}