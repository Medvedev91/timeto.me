import SwiftUI

/**
 * Resistant Picker to view model state re-render
 * Based on https://stackoverflow.com/a/62655072
 */
struct PickerStable: UIViewRepresentable {

    class Coordinator: NSObject, UIPickerViewDataSource, UIPickerViewDelegate {

        @Binding var selection: Int

        var initialSelection: Int?
        var titleForRow: (Int) -> String
        var rowCount: Int

        func numberOfComponents(in pickerView: UIPickerView) -> Int {
            1
        }

        func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
            rowCount
        }

        func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
            titleForRow(row)
        }

        func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
            selection = row
        }

        init(selection: Binding<Int>, titleForRow: @escaping (Int) -> String, rowCount: Int) {
            self.titleForRow = titleForRow
            self._selection = selection
            self.rowCount = rowCount
        }
    }

    @Binding var selection: Int

    var rowCount: Int
    let titleForRow: (Int) -> String

    func makeCoordinator() -> Coordinator {
        Coordinator(selection: $selection, titleForRow: titleForRow, rowCount: rowCount)
    }

    func makeUIView(context: UIViewRepresentableContext<PickerStable>) -> UIPickerView {
        let view = UIPickerView()
        view.delegate = context.coordinator
        view.dataSource = context.coordinator
        return view
    }

    func updateUIView(_ uiView: UIPickerView, context: UIViewRepresentableContext<PickerStable>) {

        context.coordinator.titleForRow = titleForRow
        context.coordinator.rowCount = rowCount

        //only update selection if it has been changed
        if context.coordinator.initialSelection != selection {
            uiView.selectRow(selection, inComponent: 0, animated: true)
            context.coordinator.initialSelection = selection
        }
    }
}
