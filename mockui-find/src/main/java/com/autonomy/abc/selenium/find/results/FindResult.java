package com.autonomy.abc.selenium.find.results;

import com.autonomy.abc.selenium.find.preview.DocumentPreviewer;
import com.autonomy.abc.selenium.query.QueryResult;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Calendar;

public class FindResult extends QueryResult {
    FindResult(final WebElement result, final WebDriver driver){
        super(result, driver);
    }

    @Override
    public WebElement title() {
        return findElement(By.xpath(".//*[@class[contains(.,'result-header')] and text()[normalize-space()]]"));
    }

    public String link() {
        return findElement(By.cssSelector("a")).getAttribute("href");
    }

    @Override
    public WebElement icon() {
        return findElement(By.cssSelector(".content-type i"));
    }

    public String getReference() {
        return findElement(By.className("document-reference")).getText();
    }

    public String getDate(){return findElement(By.className("document-date")).getText();}

    public WebElement similarDocuments() {
        return findElement(By.className("similar-documents-trigger"));
    }

    private WebElement previewButton(){
        return findElement(By.className("preview-link"));
    }

    private Boolean previewButtonExists(){
        return !findElements(By.className("preview-link")).isEmpty();
    }

    @Override
    public DocumentPreviewer openDocumentPreview(){
        if (previewButtonExists()){
            previewButton().click();
        } else {
            title().click();
        }

        return DocumentPreviewer.make(getDriver());
    }

    public String convertDate(){
        String badFormatDate = getDate();
        final String[] words = badFormatDate.split(" ");
        final int timeAmount;
        final String timeUnit;
        if(words[0].equals("a")||words[0].equals("an")){
            timeAmount=1;
            timeUnit = words[1];
        }
        else{
            timeAmount= Integer.parseInt(words[0]);
            timeUnit = words[1];
        }

        final Calendar date = Calendar.getInstance();

        switch (timeUnit) {
            case "minute":
            case "minutes":
                date.add(Calendar.MINUTE,-timeAmount);
                break;

            case "hour":
            case "hours":
                date.add(Calendar.HOUR_OF_DAY, -timeAmount);
                break;

            case "day":
            case "days":
                date.add(Calendar.DAY_OF_MONTH,-timeAmount);
                break;

            case "month":
            case "months":
                date.add(Calendar.MONTH,-timeAmount);
                break;

            case "year":
            case "years":
                date.add(Calendar.YEAR,-timeAmount);
                break;
        }
        date.set(Calendar.SECOND,0);
        return date.getTime().toString();
    }

}
