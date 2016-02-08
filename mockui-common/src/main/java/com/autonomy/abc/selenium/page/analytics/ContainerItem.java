package com.autonomy.abc.selenium.page.analytics;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Comparator;

public class ContainerItem {

    private String term;
    private int searches;
    private WebElement element;

    public ContainerItem(WebElement element){
        this.element = element;
        this.term = element.findElement(By.tagName("a")).getText();
        this.searches = Integer.parseInt(element.findElement(By.tagName("span")).getText());
    }

    public String getTerm() {
        return term;
    }

    public int getSearchCount() {
        return searches;
    }

    public WebElement getElement() {
        return element;
    }

    public void click() {
        if (element == null) {
            throw new IllegalStateException("This term is not clickable");
        }
        element.findElement(By.tagName("a")).click();
    }

    @Override
    public String toString() {
        return "ContainerItem<" + term + "," + searches + ">";
    }

    public final static Comparator<ContainerItem> COUNT_ASCENDING = new Comparator<ContainerItem>() {
        @Override
        public int compare(ContainerItem o1, ContainerItem o2) {
            return o1.getSearchCount() - o2.getSearchCount();
        }

        @Override
        public String toString() {
            return "by count, ascending";
        }
    };

    public final static Comparator<ContainerItem> COUNT_DESCENDING = new Comparator<ContainerItem>() {
        @Override
        public int compare(ContainerItem o1, ContainerItem o2) {
            return COUNT_ASCENDING.compare(o2, o1);
        }

        @Override
        public String toString() {
            return "by count, descending";
        }
    };
}
