package jas.gui.display.hiderules;

import jas.gui.GuiLog;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import com.google.gson.JsonObject;

public class HideExpression {
    private Evaluator evaluator;
    private String hideExpression;
    private int ticksUnchanged = 0;
    private boolean shouldHide = false;

    public boolean shouldHide() {
        return shouldHide;
    }

    public HideExpression() {
        setExpression("");
        shouldHide = false;
    }

    public HideExpression setExpression(String expression) {
        if (expression == null) {
            expression = "";
        }
        expression = expression.trim();
        if ("".equals(expression)) {
            evaluator = new Evaluator();
        } else {
            evaluator = new Evaluator();
            try {
                evaluator.parse(expression);
            } catch (EvaluationException e) {
                throw new IllegalArgumentException("Invalid expression statement");
            }
        }
        this.hideExpression = expression;
        return this;
    }

    public String getExpression() {
        return hideExpression;
    }

    public boolean isExpressionValid(String expression) {
        if (expression == null) {
            return false;
        }
        expression = expression.trim();
        if ("".equals(expression)) {
            return true;
        }

        evaluator = new Evaluator();
        try {
            evaluator.parse(expression);
            evaluator.putVariable("count", Integer.toString(0));
            evaluator.putVariable("prevCount", Integer.toString(0));
            evaluator.putVariable("maxCount", Integer.toString(1));
            evaluator.putVariable("unchanged", Integer.toString(0));
            evaluator.evaluate();
        } catch (EvaluationException e) {
            return false;
        }
        return true;
    }

    public void update(Integer trackedCount, Integer prevTrackedCount, Integer maxCount, int ticksPerUpdate) {
        prevTrackedCount = prevTrackedCount != null ? prevTrackedCount : 0;
        ticksUnchanged = trackedCount.equals(prevTrackedCount) ? ticksUnchanged + ticksPerUpdate : 0;
        if ("".equals(hideExpression)) {
            shouldHide = false;
        } else {
            evaluator.putVariable("count", Integer.toString(trackedCount));
            evaluator.putVariable("prevCount", Integer.toString(prevTrackedCount));
            evaluator.putVariable("maxCount", Integer.toString(maxCount));
            evaluator.putVariable("unchanged", Integer.toString(ticksUnchanged));
            try {
                String eval = evaluator.evaluate();
                shouldHide = "1.0".equals(eval);
            } catch (EvaluationException e) {
                shouldHide = false;
                GuiLog.log().severe("Failed to evaluate expression %s", hideExpression);
                e.printStackTrace();
            }
        }
    }

    public void saveCustomData(JsonObject jsonObject) {

    }

    public void loadCustomData(JsonObject customData) {

    }
}
