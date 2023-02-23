import SwiftUI
import shared

struct TriggersView__Form: View {

    @State private var allTriggers: [Trigger] = [] // Fills in onAppear()

    let triggers: [Trigger]
    let onTriggersChanged: ([Trigger]) -> Void

    var spaceAround = 16.0 // todo everywhere
    var bgColor = UIColor.myBackground // todo everywhere
    var paddingTop = 0.0

    var body: some View {

        VStack(spacing: 0) {

            if !allTriggers.isEmpty {

                ScrollView(.horizontal, showsIndicators: false) {

                    HStack(spacing: 0) {

                        MySpacerSize(width: spaceAround)

                        let triggersSorted = allTriggers.sorted { t1, t2 in
                            let isT1Selected = triggers.map { $0.id }.contains(t1.id)
                            let isT2Selected = triggers.map { $0.id }.contains(t2.id)
                            if isT1Selected == isT2Selected {
                                if t1.typeSortAsc != t2.typeSortAsc {
                                    return t1.typeSortAsc < t2.typeSortAsc
                                }
                                return t1.id < t2.id
                            }
                            return isT1Selected
                        }

                        ForEach(triggersSorted, id: \.id) { trigger in

                            MySpacerSize(width: triggersSorted.first !== trigger ? 8 : 0)

                            let isSelected = triggers.map { $0.id }.contains(trigger.id)

                            Button(
                                    action: {
                                        withAnimation {
                                            var newTriggers = triggers
                                            if isSelected {
                                                newTriggers.removeAll { $0.id == trigger.id }
                                            } else {
                                                newTriggers.append(trigger)
                                            }
                                            onTriggersChanged(newTriggers)
                                        }
                                    },
                                    label: {
                                        Text(trigger.title)
                                                .foregroundColor(isSelected ? .white : .blue)
                                                .font(.system(size: 14, weight: .semibold))
                                                .padding(.vertical, 6)
                                                .padding(.horizontal, 11)
                                                .background(
                                                        ZStack {
                                                            Capsule(style: .circular).fill(.blue)
                                                            Capsule(style: .circular).fill(isSelected ? .blue : Color(bgColor)).padding(.all, 1)
                                                        }
                                                )
                                    }
                            )
                        }

                        MySpacerSize(width: spaceAround)
                    }
                }
                        .padding(.top, paddingTop)
            }
        }
                .frame(maxWidth: .infinity)
                .onAppear {
                    allTriggers = []
                    DI.checklists.forEach { checklist in
                        allTriggers.append(Trigger.Checklist(checklist: checklist))
                    }
                    DI.shortcuts.forEach { shortcut in
                        allTriggers.append(Trigger.Shortcut(shortcut: shortcut))
                    }
                }
    }
}

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
                    if let trigger = trigger as? Trigger.Checklist {
                        checklist = trigger.checklist
                        isChecklistPresented = true
                    } else if let trigger = trigger as? Trigger.Shortcut {
                        performShortcutOrError(trigger.shortcut) { error in
                            UtilsKt.showUiAlert(message: error, reportApiText: nil)
                        }
                    } else {
                        fatalError("TriggersView__ListItem")
                    }
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
