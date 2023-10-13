import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import SpeciesCard from "../SpeciesCard";

test('card displays species and description', () => {
    const expectedSpecies = "ExpectedSpecies";
    const expectedDesc = "ExpectedDescription";

    render(<SpeciesCard species={expectedSpecies} description={expectedDesc} traits="" selected={false} />);

    const titleElt = screen.getByText(expectedSpecies);
    const descriptionElt = screen.getByText(expectedDesc);

    expect(titleElt).toBeTruthy();
    expect(descriptionElt).toBeTruthy();
});

test('card displays traits on popover click', async () => {
    const expectedTitle = 'Traits';
    const expectedTraits = "ExpectedTraits";

    const { container } = render(<SpeciesCard species="" description="" traits={expectedTraits} selected={true} />);

    const traitsIcon = container.querySelector('.anticon-info-circle');
    expect(traitsIcon).toBeTruthy();

    if (traitsIcon != null) {
        fireEvent.mouseOver(traitsIcon);
    }

    await waitFor(() => screen.getByText(expectedTitle));

    expect(screen.getByText(expectedTitle)).toBeTruthy();
    expect(screen.getByText(expectedTraits)).toBeTruthy();
})

test('selected card has correct styling', () => {
    const { container } = render(<SpeciesCard species="" description="" traits="" selected={true} />);

    expect(container.firstChild).toHaveClass('speciesCard-selected');
})