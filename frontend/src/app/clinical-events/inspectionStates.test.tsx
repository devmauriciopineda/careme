import { fireEvent, render, screen } from "@testing-library/react";

import ErrorState from "./error";
import Loading from "./loading";

describe("clinical event inspection states", () => {
    it("announces loading while the history is being read", () => {
        render(<Loading />);

        expect(screen.getByRole("status")).toHaveAttribute("aria-busy", "true");
        expect(screen.getByText("Cargando historia clínica…")).toBeInTheDocument();
    });

    it("announces a failure and retries through its accessible action", () => {
        const reset = vi.fn();

        render(<ErrorState error={new Error("network failure")} reset={reset} />);

        expect(screen.getByRole("alert")).toHaveTextContent("No pudimos conectar con el servidor");
        fireEvent.click(screen.getByRole("button", { name: "Reintentar" }));
        expect(reset).toHaveBeenCalledOnce();
    });
});