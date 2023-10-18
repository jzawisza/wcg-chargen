import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import SelectableCard from "../SelectableCard";

test('card displays title and description', () => {
    const expectedTitle = "ExpectedTitle";
    const expectedDesc = "ExpectedDescription";

    render(<SelectableCard title={expectedTitle} description={expectedDesc} features="" selected={false} />);

    const titleElt = screen.getByText(expectedTitle);
    const descriptionElt = screen.getByText(expectedDesc);

    expect(titleElt).toBeTruthy();
    expect(descriptionElt).toBeTruthy();
});

test('card displays features on popover hover', async () => {
    const expectedPopoverTitle = 'Features';
    const expectedFeatures = "ExpectedFeatures";

    const { container } = render(<SelectableCard title="" description="" features={expectedFeatures} selected={false} />);

    const infoIcon = container.querySelector('.anticon-info-circle');
    expect(infoIcon).toBeTruthy();

    if (infoIcon != null) {
        fireEvent.mouseOver(infoIcon);
    }

    await waitFor(() => screen.getByText(expectedPopoverTitle));

    expect(screen.getByText(expectedPopoverTitle)).toBeTruthy();
    expect(screen.getByText(expectedFeatures)).toBeTruthy();
})

test('selected card has correct styling', () => {
    const { container } = render(<SelectableCard title="" description="" features="" selected={true} />);

    expect(container.firstChild).toHaveClass('selectableCard-selected');
})