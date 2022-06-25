package ch.qos.logback.core.joran.spi;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.OptionHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SimpleRuleStore extends ContextAwareBase implements RuleStore {
   static String KLEENE_STAR = "*";
   HashMap<ElementSelector, List<Action>> rules = new HashMap();

   public SimpleRuleStore(Context context) {
      this.setContext(context);
   }

   @Override
   public void addRule(ElementSelector elementSelector, Action action) {
      action.setContext(this.context);
      List<Action> a4p = (List)this.rules.get(elementSelector);
      if (a4p == null) {
         a4p = new ArrayList();
         this.rules.put(elementSelector, a4p);
      }

      a4p.add(action);
   }

   @Override
   public void addRule(ElementSelector elementSelector, String actionClassName) {
      Action action = null;

      try {
         action = (Action)OptionHelper.instantiateByClassName(actionClassName, Action.class, this.context);
      } catch (Exception var5) {
         this.addError("Could not instantiate class [" + actionClassName + "]", var5);
      }

      if (action != null) {
         this.addRule(elementSelector, action);
      }

   }

   @Override
   public List<Action> matchActions(ElementPath elementPath) {
      List<Action> actionList;
      if ((actionList = this.fullPathMatch(elementPath)) != null) {
         return actionList;
      } else if ((actionList = this.suffixMatch(elementPath)) != null) {
         return actionList;
      } else if ((actionList = this.prefixMatch(elementPath)) != null) {
         return actionList;
      } else {
         return (actionList = this.middleMatch(elementPath)) != null ? actionList : null;
      }
   }

   List<Action> fullPathMatch(ElementPath elementPath) {
      for(ElementSelector selector : this.rules.keySet()) {
         if (selector.fullPathMatch(elementPath)) {
            return (List<Action>)this.rules.get(selector);
         }
      }

      return null;
   }

   List<Action> suffixMatch(ElementPath elementPath) {
      int max = 0;
      ElementSelector longestMatchingElementSelector = null;

      for(ElementSelector selector : this.rules.keySet()) {
         if (this.isSuffixPattern(selector)) {
            int r = selector.getTailMatchLength(elementPath);
            if (r > max) {
               max = r;
               longestMatchingElementSelector = selector;
            }
         }
      }

      return longestMatchingElementSelector != null ? (List)this.rules.get(longestMatchingElementSelector) : null;
   }

   private boolean isSuffixPattern(ElementSelector p) {
      return p.size() > 1 && p.get(0).equals(KLEENE_STAR);
   }

   List<Action> prefixMatch(ElementPath elementPath) {
      int max = 0;
      ElementSelector longestMatchingElementSelector = null;

      for(ElementSelector selector : this.rules.keySet()) {
         String last = selector.peekLast();
         if (this.isKleeneStar(last)) {
            int r = selector.getPrefixMatchLength(elementPath);
            if (r == selector.size() - 1 && r > max) {
               max = r;
               longestMatchingElementSelector = selector;
            }
         }
      }

      return longestMatchingElementSelector != null ? (List)this.rules.get(longestMatchingElementSelector) : null;
   }

   private boolean isKleeneStar(String last) {
      return KLEENE_STAR.equals(last);
   }

   List<Action> middleMatch(ElementPath path) {
      int max = 0;
      ElementSelector longestMatchingElementSelector = null;

      for(ElementSelector selector : this.rules.keySet()) {
         String last = selector.peekLast();
         String first = null;
         if (selector.size() > 1) {
            first = selector.get(0);
         }

         if (this.isKleeneStar(last) && this.isKleeneStar(first)) {
            List<String> copyOfPartList = selector.getCopyOfPartList();
            if (copyOfPartList.size() > 2) {
               copyOfPartList.remove(0);
               copyOfPartList.remove(copyOfPartList.size() - 1);
            }

            int r = 0;
            ElementSelector clone = new ElementSelector(copyOfPartList);
            if (clone.isContainedIn(path)) {
               r = clone.size();
            }

            if (r > max) {
               max = r;
               longestMatchingElementSelector = selector;
            }
         }
      }

      return longestMatchingElementSelector != null ? (List)this.rules.get(longestMatchingElementSelector) : null;
   }

   public String toString() {
      String TAB = "  ";
      StringBuilder retValue = new StringBuilder();
      retValue.append("SimpleRuleStore ( ").append("rules = ").append(this.rules).append("  ").append(" )");
      return retValue.toString();
   }
}
