import SwiftUI
import shared

struct TaskFormSheet: View {

    @Binding private var isPresented: Bool
    private var task: TaskModel

    @EnvironmentObject private var timetoAlert: TimetoAlert

    @StateObject private var triggersState: Triggers__State

    init(isPresented: Binding<Bool>, task: TaskModel) {
        self.task = task
        _isPresented = isPresented
        _triggersState = StateObject(wrappedValue: Triggers__State(text: task.text))
    }

    var body: some View {

        VStack {

            HStack {

                Button(
                        action: { isPresented.toggle() },
                        label: { Text("Cancel") }
                )
                        .padding(.leading, 25)

                Spacer()

                Button(
                        action: {
                            do {
                                task.upTextWithValidation(newText: triggersState.textWithTriggers()) { _ in
                                    // todo
                                    isPresented = false
                                }
                            } catch let error as MyError {
                                timetoAlert.alert(error.message)
                                return
                            } catch {
                                fatalError()
                            }
                        },
                        label: {
                            Text("Save")
                                    .fontWeight(.heavy)
                                    .padding(.trailing, 25)
                        }
                )
                        .disabled(triggersState.text.isEmpty)
            }
                    .padding(.top, 20)

            TriggersView__Form__Deprecated(state: triggersState)
                    .padding(.top, 10)
                    .padding(.leading, 10)

            MyTextEditor(
                    text: $triggersState.text,
                    placeholder: "Task"
            )
                    .padding(.leading, 25)
                    .padding(.trailing, 25)
                    // The height and padding should not be large because
                    // there is not enough space to show the WheelPicker
                    .padding(.top, 15)
                    .frame(height: 60)

            Spacer()
        }
    }
}
