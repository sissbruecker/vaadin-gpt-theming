package de.sissbruecker.gpttheming.services;

import com.helger.commons.collection.impl.ICommonsList;
import com.helger.css.ECSSVersion;
import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSKeyframesRule;
import com.helger.css.decl.CSSStyleRule;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.decl.shorthand.CSSShortHandDescriptor;
import com.helger.css.decl.shorthand.CSSShortHandRegistry;
import com.helger.css.property.ECSSProperty;
import com.helger.css.reader.CSSReader;
import com.helger.css.writer.CSSWriter;
import com.helger.css.writer.CSSWriterSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CssModifier {

    private final CascadingStyleSheet stylesheet;

    public CssModifier(String css) {
        stylesheet = CSSReader.readFromString(css, ECSSVersion.LATEST);
    }

    public void updateCss(String cssUpdate) {
        CascadingStyleSheet updateStyleSheet = CSSReader.readFromString(cssUpdate, ECSSVersion.LATEST);
        CSSWriterSettings writerSettings = new CSSWriterSettings();

        // Merge style rules
        List<CSSStyleRule> updatedRules = getAllStyleRules(updateStyleSheet);
        ICommonsList<CSSStyleRule> existingRules = stylesheet.getAllStyleRules();

        for (CSSStyleRule updatedRule : updatedRules) {
            boolean ruleUpdated = false;
            for (CSSStyleRule existingRule : existingRules) {
                if (existingRule.getSelectorsAsCSSString(writerSettings, 0).equals(updatedRule.getSelectorsAsCSSString(writerSettings, 0))) {
                    // Merge properties from new rule into existing rule
                    List<CSSDeclaration> updatedDeclarations = getAllPropertyDeclarations(updatedRule);
                    for (CSSDeclaration updatedDeclaration : updatedDeclarations) {
                        boolean declarationUpdated = false;
                        for (CSSDeclaration existingDeclaration : existingRule.getAllDeclarations()) {
                            if (existingDeclaration.getProperty().equals(updatedDeclaration.getProperty())) {
                                // Update existing declaration with value from new declaration
                                existingDeclaration.setExpression(updatedDeclaration.getExpression());
                                declarationUpdated = true;
                                break;
                            }
                        }
                        if (!declarationUpdated) {
                            // Add new declaration to existing rule
                            existingRule.addDeclaration(updatedDeclaration);
                        }
                    }
                    ruleUpdated = true;
                    break;
                }
            }
            if (!ruleUpdated) {
                // Add new rule to existing stylesheet
                stylesheet.addRule(updatedRule);
            }
        }

        // Merge keyframe rules
        ICommonsList<CSSKeyframesRule> updatedKeyframes = updateStyleSheet.getAllKeyframesRules();
        ICommonsList<CSSKeyframesRule> existingKeyframes = stylesheet.getAllKeyframesRules();

        for (CSSKeyframesRule updatedRule : updatedKeyframes) {
            boolean keyframeUpdated = false;

            for (CSSKeyframesRule existingRule : existingKeyframes) {
                if (Objects.equals(existingRule.getAnimationName(), updatedRule.getAnimationName())) {
                    keyframeUpdated = true;
                    existingRule.removeAllBlocks();
                    updatedRule.getAllBlocks().forEach(existingRule::addBlock);
                }
            }

            if (!keyframeUpdated) {
                stylesheet.addRule(updatedRule);
            }
        }
    }

    private List<CSSStyleRule> getAllStyleRules(CascadingStyleSheet styleSheet) {
        ICommonsList<CSSStyleRule> allStyleRules = styleSheet.getAllStyleRules();
        List<CSSStyleRule> result = new ArrayList<>();

        allStyleRules.forEach(rule -> {
            if (rule.getSelectorCount() == 1) {
                result.add(rule);
                return;
            }
            // Split up selector lists into individual selectors
            rule.getAllSelectors().forEach(cssSelector -> {
                CSSStyleRule extractedRule = new CSSStyleRule();
                extractedRule.addSelector(cssSelector);
                rule.getAllDeclarations().forEach(extractedRule::addDeclaration);
                result.add(extractedRule);
            });
        });

        return result;
    }

    private static final List<ECSSProperty> SHORTHANDS = List.of(ECSSProperty.BORDER, ECSSProperty.BACKGROUND);

    private List<CSSDeclaration> getAllPropertyDeclarations(CSSStyleRule rule) {
        ICommonsList<CSSDeclaration> allDeclarations = rule.getAllDeclarations();
        List<CSSDeclaration> result = new ArrayList<>();

        allDeclarations.forEach(cssDeclaration -> {
            ECSSProperty shorthand = SHORTHANDS.stream().filter(cssDeclaration::hasProperty).findFirst().orElse(null);

            if (shorthand == null) {
                result.add(cssDeclaration);
                return;
            }

            CSSShortHandDescriptor shortHandDescriptor = CSSShortHandRegistry.getShortHandDescriptor(shorthand);
            ICommonsList<CSSDeclaration> splitIntoPieces = shortHandDescriptor.getSplitIntoPieces(cssDeclaration);
            result.addAll(splitIntoPieces);
        });

        return result;
    }

    public String getCss() {
        CSSWriter writer = new CSSWriter().setWriteHeaderText(false);
        writer.getSettings().setOptimizedOutput(false);
        return writer.getCSSAsString(stylesheet);
    }
}
