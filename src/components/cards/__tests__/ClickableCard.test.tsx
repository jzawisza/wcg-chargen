import { render, screen } from "@testing-library/react";
import ClickableCard from "../ClickableCard";

test('card displays title and description', () => {
    const expectedTitle = "ExpectedTitle";
    const expectedDesc = "ExpectedDescription";

    render(<ClickableCard title={expectedTitle} description={expectedDesc} />);

    const titleElt = screen.getByText(expectedTitle);
    const descriptionElt = screen.getByText(expectedDesc);

    expect(titleElt).toBeTruthy();
    expect(descriptionElt).toBeTruthy();
});

test('card correctly renders children', () => {
    const dataTestId = "ChildElement";

    render(<ClickableCard title="foo" description="bar"><div data-testid={dataTestId} /></ClickableCard>);

    const childElt = screen.getByTestId(dataTestId);
    
    expect(childElt).toBeTruthy();
});