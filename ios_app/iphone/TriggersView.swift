import SwiftUI
import shared

struct TriggersView__List: View {

    let triggers: [Trigger]
    var spaceBetween = 8.0
    var paddingTop = 0.0
    var paddingBottom = 0.0
    var contentPaddingStart = 16.0
    var contentPaddingEnd = 16.0

    var body: some View {

        if triggers.isEmpty {
            EmptyView()
        } else {

            ScrollView(.horizontal, showsIndicators: false) {

                HStack(spacing: 0) {

                    MySpacerSize(width: contentPaddingStart)

                    ForEach(triggers, id: \.id) { trigger in
                        TriggersView__ListItem(trigger: trigger)
                                .padding(.trailing, triggers.last !== trigger ? spaceBetween : 0)
                    }

                    MySpacerSize(width: contentPaddingEnd)
                }
                        .frame(maxWidth: .infinity)
            }
                    .padding(.top, paddingTop)
                    .padding(.bottom, paddingBottom)
        }
    }
}

struct TriggersView__ListItem: View {

    let trigger: Trigger

    @State private var isChecklistPresented = false
    /// # PROVOKE_STATE_UPDATE
    @State private var checklist: ChecklistModel? = nil

    var body: some View {
        Button(
                action: {
                    trigger.performUI()
                },
                label: {
                    Text(trigger.title)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(trigger.getColor().toColor())
                            .clipShape(Capsule())
                            .foregroundColor(.white)
                            .font(.system(size: 14))

                    /// # PROVOKE_STATE_UPDATE
                    EmptyView().id(checklist?.name ?? "")
                }
        )
                .sheetEnv(isPresented: $isChecklistPresented) {
                    if let checklist = checklist {
                        ChecklistDialog(isPresented: $isChecklistPresented, checklist: checklist)
                    }
                }
    }
}
