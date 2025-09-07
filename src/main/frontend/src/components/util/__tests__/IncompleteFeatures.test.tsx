import { render } from "@testing-library/react";
import IncompleteFeatures from "../IncompleteFeatures";

test('no Tier I or Tier II features displayed if feature lists from props are empty', () => {
    const { queryByText } = render(<IncompleteFeatures tier1Features={[]} tier2Features={[]} />);

    expect(queryByText(/Tier I Features:/)).not.toBeInTheDocument();
    expect(queryByText(/Tier II Features:/)).not.toBeInTheDocument();
})

test('Tier I features displayed if Tier I feature list from props is non-empty', () => {
    const tier1Features = ['Feature 1', 'Feature 2', 'Feature 3'];
    const { queryByText } = render(<IncompleteFeatures tier1Features={tier1Features} tier2Features={[]} />);

    expect(queryByText(/Tier I Features:/)).toBeInTheDocument();
    expect(queryByText(/Feature 1/)).toBeInTheDocument();
    expect(queryByText(/Feature 2/)).toBeInTheDocument();
    expect(queryByText(/Feature 3/)).toBeInTheDocument();
    expect(queryByText(/Tier II Features:/)).not.toBeInTheDocument();
});

test('Tier II features displayed if Tier II feature list from props is non-empty', () => {
    const tier2Features = ['Feature A', 'Feature B', 'Feature C'];
    const { queryByText } = render(<IncompleteFeatures tier1Features={[]} tier2Features={tier2Features} />);

    expect(queryByText(/Tier II Features:/)).toBeInTheDocument();
    expect(queryByText(/Feature A/)).toBeInTheDocument();
    expect(queryByText(/Feature B/)).toBeInTheDocument();
    expect(queryByText(/Feature C/)).toBeInTheDocument();
    expect(queryByText(/Tier I Features:/)).not.toBeInTheDocument();
});